package com.example.firebasetwitteroauth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        FirebaseAuth.getInstance().signInAnonymously()
//            .addOnCompleteListener(this) { task ->
//                Log.d(TAG, "isSuccessful " + task.isSuccessful)
//                if (!task.isSuccessful)
//                    Log.d(TAG, "taskexception:", task.exception)
//            }

        var oAuthProviderBuilder = OAuthProvider.newBuilder("twitter.com")
        var auth = FirebaseAuth.getInstance()
        auth.pendingAuthResult?.apply {
            addOnSuccessListener {
                Log.d(TAG, "pending success:" + it)
            }
            addOnFailureListener {
                Log.d(TAG, "pending failure:" + it)
            }
        } ?:
            auth
                .startActivityForSignInWithProvider(this, oAuthProviderBuilder.build())
                .addOnSuccessListener {
                    Log.d(TAG, "success:" + it)
                    var credential = it.credential as OAuthCredential;

                    Log.d(TAG, "username:" + it.additionalUserInfo?.username)
                    Log.d(TAG, "profile.id:" + it.additionalUserInfo?.profile?.get("id"))
                    Log.d(TAG, "accessToken:" + credential.accessToken)
                    Log.d(TAG, "secret:" + credential.secret)
                    Log.d(TAG, "user.uid:" + it.user?.uid)

                }
                .addOnFailureListener {
                    Log.d(TAG, "failure:" + it)
                }

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
