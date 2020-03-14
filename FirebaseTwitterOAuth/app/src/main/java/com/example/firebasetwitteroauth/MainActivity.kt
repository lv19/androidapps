package com.example.firebasetwitteroauth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener(this) { task ->
                Log.d(TAG, "isSuccessful " + task.isSuccessful)
                if (!task.isSuccessful)
                    Log.d(TAG, "taskexception:", task.exception)
            }

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
