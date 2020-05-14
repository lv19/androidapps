package com.example.android.kfirestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel: MainViewModel by viewModels()
        viewModel.getCs("1").observe(this, Observer {
            Log.i("MainActivity", "getCs=$it")
        })
    }

//    override fun onStart() {
//        super.onStart()
//        var firestore = Firebase.firestore
//        val csRef = firestore.collection("tacs").document("1")
//        csRef.get()
//            .addOnSuccessListener {
//                Log.d("MainActivity", "document id=${it.id} data=${it.data}")
//                val cs = it["cs"]
//                Log.d("MainActivity", "document cs=$cs")
//            }
//
//        val tacsRef = firestore.collection("tacs")
//        tacsRef.whereArrayContains("cs", "1").get()
//            .addOnSuccessListener {
//                val ids = it.documents.map { it.id }
//                Log.d("MainActivity", "document ids=$ids")
//            }
//
//
//        csRef.set(hashMapOf("cs" to listOf("3", "6")))
//            .addOnSuccessListener {
//                Log.d("MainActivity", "set success")
//            }
//    }

}

class MainViewModel() : ViewModel() {
    private val mainRepository = MainRepository()
    fun getCs(userId: String) = mainRepository.getCs(userId)
}

class MainRepository() {

    val firestore = Firebase.firestore

    var cs: MutableLiveData<List<String>> = MutableLiveData()

    fun getCs(userId: String): LiveData<List<String>> {
        val csRef = firestore.collection("tacs").document(userId)
        csRef.get()
            .addOnSuccessListener {
                Log.d("MainRepository", "document id=${it.id} data=${it.data}")

                cs.value = (it["cs"] as ArrayList<String>)

                Log.d("MainRepository", "document cs.value=${cs.value}")
            }
        return cs
    }
}