package com.example.android.kfirestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        var firestore = Firebase.firestore
        val csRef = firestore.collection("tacs").document("1")
        csRef.get()
            .addOnSuccessListener {
                Log.d("MainActivity", "document id=${it.id} data=${it.data}")
                val cs = it["cs"]
                Log.d("MainActivity", "document cs=$cs")
            }

        val tacsRef = firestore.collection("tacs")
        tacsRef.whereArrayContains("cs", "1").get()
            .addOnSuccessListener {
                val ids = it.documents.map { it.id }
                Log.d("MainActivity", "document ids=$ids")
            }


        csRef.set(hashMapOf("cs" to listOf("3", "6")))
            .addOnSuccessListener {
                Log.d("MainActivity", "set success")
            }
    }
}
