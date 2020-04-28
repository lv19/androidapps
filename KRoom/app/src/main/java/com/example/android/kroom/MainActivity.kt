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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userAdapter = UserAdapter()
        recyclerView.adapter = userAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val userViewModel: UserViewModel by viewModels()
        //userViewModel.insert(User(1, "Uno"))
        //userViewModel.insert(User(2, "Dos"))
        userViewModel.users.observe(this, Observer {
            it.forEach{user ->Log.i("MainActivity", "user=$user")}
            recyclerView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            userAdapter.setUsers(it)
        })
    }
}

class UserAdapter(): RecyclerView.Adapter<UserAdapter.ViewHolder>(){
    private var users = emptyList<User>()

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.textViewName.text = users[position].name
    }

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }
}

@Entity
data class User(@PrimaryKey val id: Long, val name: String)

@Dao
interface UserDao {
    @Query("select * from user")
    fun getUsers(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
}

class UserRepository(context: Context) {
    private val userDao: UserDao = KRoomDatabase.getInstance(context).userDao()

    val users = userDao.getUsers()

    suspend fun insert(user: User) = userDao.insert(user)
}

@Database(entities = [User::class], version = 1, exportSchema = false)
public abstract class KRoomDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao

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

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val userRepository: UserRepository = UserRepository(application)

    val users = userRepository.users

    fun insert(user: User) = viewModelScope.launch {
        userRepository.insert(user)
    }
}
