package com.example.firebasetwitteroauth

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import lvlambda.twitter.TwitterApiBuilder
import lvlambda.twitter.TwitterOAuth
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.absoluteValue
import kotlin.random.Random

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

//        val oAuthProviderBuilder = OAuthProvider.newBuilder("twitter.com")
//        val auth = FirebaseAuth.getInstance()
//        auth.pendingAuthResult?.apply {
//            addOnSuccessListener {
//                Log.d(TAG, "pending success:" + it)
//            }
//            addOnFailureListener {
//                Log.d(TAG, "pending failure:" + it)
//            }
//        } ?:
//        auth.startActivityForSignInWithProvider(this, oAuthProviderBuilder.build())
//            .addOnSuccessListener {
//                Log.d(TAG, "success:" + it)
//                val credential = it.credential as OAuthCredential;
//                val username = it.additionalUserInfo?.username ?: ""
//                val accessToken = credential.accessToken
//                val secret = credential.secret ?: ""
//                Log.d(TAG, "username:$username")
//                Log.d(TAG, "profile.id:" + it.additionalUserInfo?.profile?.get("id"))
//                Log.d(TAG, "accessToken:$accessToken")
//                Log.d(TAG, "secret:$secret")
//                Log.d(TAG, "user.uid:" + it.user?.uid)
//                Log.d(TAG, "consumer_key:${OAuthConst.oauth_consumer_key}")
//                Log.d(TAG, "consumerSecret:${OAuthConst.consumerSecret}")
//                val twitterApi = TwitterApiBuilder(
//                    username,
//                    OAuthConst.oauth_consumer_key,
//                    OAuthConst.consumerSecret,
//                    accessToken,
//                    secret
//                ).create(TwitterApi::class.java)
//                getListFriends(twitterApi)
//                getListFollowers(twitterApi)
//            }
//            .addOnFailureListener {
//                Log.d(TAG, "failure:" + it)
//            }

        val twitterApi = TwitterApiBuilder(
            TwitterAccessToken.username,
            OAuthConst.oauth_consumer_key,
            OAuthConst.consumerSecret,
            TwitterAccessToken.accessToken,
            TwitterAccessToken.secret
        ).create(TwitterApi::class.java)
        getListFriends(twitterApi)
        getListFollowers(twitterApi)
    }

    private fun getListFriends(twitterApi: TwitterApi) {
        twitterApi.friendsIds().enqueue(object : Callback<TwitterUsersIds> {
            override fun onFailure(call: Call<TwitterUsersIds>, t: Throwable) {
                Log.d(TAG, t?.message)
            }

            override fun onResponse(call: Call<TwitterUsersIds>, response: Response<TwitterUsersIds>) {
                Log.d(TAG, "getListFriends response.code:${response.code()}")
                //Log.d(TAG, "response.headers:${response.headers()}")
                val twitterUsersIds = response.body()
                twitterUsersIds?.let {
                    twitterUsersIds.ids?.forEach { Log.d(TAG, "id:$it") }
                    Log.d(TAG, "next_cursor:${twitterUsersIds.next_cursor}")
                    Log.d(TAG, "previous_cursor:${twitterUsersIds.previous_cursor}")
                    twitterUsersIds.ids?.let {
                        it.chunked(TwitterApi.MAX_LOOKUP_IDS).forEach {
                            getListUsers(twitterApi, it.joinToString(","))
                        }
                    }
                }

            }
        } )
    }

    private fun getListFollowers(twitterApi: TwitterApi) {
        twitterApi.followersIds().enqueue(object : Callback<TwitterUsersIds> {
            override fun onFailure(call: Call<TwitterUsersIds>, t: Throwable) {
                Log.d(TAG, t?.message)
            }

            override fun onResponse(call: Call<TwitterUsersIds>, response: Response<TwitterUsersIds>) {
                Log.d(TAG, "getListFollowers response.code:${response.code()}")
                //Log.d(TAG, "response.headers:${response.headers()}")
                val twitterUsersIds = response.body()
                twitterUsersIds?.let {
                    twitterUsersIds.ids?.forEach { Log.d(TAG, "id:$it") }
                    Log.d(TAG, "next_cursor:${twitterUsersIds.next_cursor}")
                    Log.d(TAG, "previous_cursor:${twitterUsersIds.previous_cursor}")
                }

            }
        } )
    }

    private fun getListUsers(twitterApi: TwitterApi, user_id:String) {
        twitterApi.usersLookup(user_id).enqueue(object : Callback<ListTwitterUser> {
            override fun onFailure(call: Call<ListTwitterUser>, t: Throwable) {
                Log.d(TAG, t?.message)
            }

            override fun onResponse(call: Call<ListTwitterUser>, response: Response<ListTwitterUser>) {
                Log.d(TAG, "getListUsers response.code:${response.code()}")
                val listUsers = response.body()
                listUsers?.let {
                    listUsers.forEach {
                        Log.d(TAG, "twitteUser:$it")
                        //it.users?.forEach{Log.d(TAG, "user:$it")}
                    }
                }
                Log.d(TAG, "size= " + listUsers?.size.toString())
            }
        })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
