package com.example.android.kroom

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_layout.view.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accountAdapter = AccountAdapter()
        recyclerView.adapter = accountAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        val accountViewModel: AccountViewModel by viewModels()
        val accounts = accountViewModel.accounts
        accounts.observe(this, Observer {
            it?.let {
                Log.i("MainActivity", "accounts=$it")
                if (it.isEmpty())
                    accountViewModel.fillDatabase()
                else
                    accountViewModel.currentAccount.value = it.first()
            }
        })

        val accountFriends = accountViewModel.accountFriends
        accountFriends.observe(this, Observer {
            it?.let {
                Log.i("MainActivity", "account=${it.account} friends=${it.friends}")
                accountAdapter.setFriends(it.friends)
            }
        })

        floatingActionButton.setOnClickListener {
            accountViewModel.currentAccount.value?.let {
                val list = accounts.value!!
                val index = list.indexOf(it)
                val newIndex = (index + 1) % list.size
                accountViewModel.currentAccount.value = list[newIndex]
            }
        }

    }
}

class AccountAdapter(): RecyclerView.Adapter<AccountAdapter.ViewHolder>(){
    private var friends = emptyList<Friend>()

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.textViewName.text = friends[position].name
    }

    fun setFriends(friends: List<Friend>) {
        this.friends = friends
        notifyDataSetChanged()
    }
}

@Entity
data class Account(@PrimaryKey val id: Long, val name: String)

@Entity(primaryKeys = ["id", "accountId"], foreignKeys = [
    ForeignKey(entity = Account::class,
        parentColumns = ["id"],
        childColumns = ["accountId"],
        onDelete = ForeignKey.CASCADE
    )
])
data class Friend(val id: Long, val accountId: Long, val name: String)

data class AccountFriends(
    @Embedded var account: Account,
    @Relation(parentColumn = "id", entityColumn = "accountId")
    var friends: List<Friend> = emptyList()
)

@Dao
interface AccountDao {
    @Query("select * from account")
    fun getAccounts(): LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Insert
    suspend fun insert(friends: List<Friend>)

    @Transaction
    @Query("select * from account where account.id = :id")
    fun getAccountFriends(id: Long): LiveData<AccountFriends>

    @Transaction
    suspend fun insert(accountFriends: AccountFriends){
        insert(accountFriends.account)
        insert(accountFriends.friends)
    }
}

class AccountRepository(context: Context) {
    private val accountDao: AccountDao = KRoomDatabase.getInstance(context).accountDao()

    val accounts = accountDao.getAccounts()

    fun getAccountFriends(id: Long) = accountDao.getAccountFriends(id)

    suspend fun insert(accountFriends: AccountFriends) = accountDao.insert(accountFriends)
}

@Database(entities = [Account::class, Friend::class], version = 1, exportSchema = false)
public abstract class KRoomDatabase: RoomDatabase() {
    abstract fun accountDao(): AccountDao

    companion object {
        private lateinit var instance: KRoomDatabase
        fun getInstance(context: Context): KRoomDatabase {
            if (!::instance.isInitialized)
                instance = Room.databaseBuilder(
                    context,
                    KRoomDatabase::class.java,
                    "kroomdatabase"
                ).build()
            return instance
        }
    }
}

class AccountViewModel(application: Application): AndroidViewModel(application) {
    private val accountRepository: AccountRepository = AccountRepository(application)

    val accounts = accountRepository.accounts
    val currentAccount = MutableLiveData<Account>()

    val accountFriends: LiveData<AccountFriends> = currentAccount.switchMap {
            account -> accountRepository.getAccountFriends(account.id)
    }

    fun insert(accountFriends: AccountFriends) = viewModelScope.launch {
        accountRepository.insert(accountFriends)
    }

    fun fillDatabase() {
        Log.i("AccountViewModel", "fillDatabase")
        val friends1 = listOf(Friend(1, 1, "A1F1x"), Friend(3, 1, "A1F3x"))
        val accountFriends1 = AccountFriends(Account(1, "AccountUnoxx"), friends1)
        val friends2 = listOf(Friend(1, 2, "A2F1"), Friend(2, 2, "A2F2x"))
        val accountFriends2 = AccountFriends(Account(2, "AccountDos"), friends2)
        insert(accountFriends1)
        insert(accountFriends2)
    }
}
