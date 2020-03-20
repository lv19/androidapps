package com.example.firebasetwitteroauth

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

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
//        auth.pendingAuthResult?.apply {
//            addOnSuccessListener {
//                Log.d(TAG, "pending success:" + it)
//            }
//            addOnFailureListener {
//                Log.d(TAG, "pending failure:" + it)
//            }
//        } ?:
        auth.startActivityForSignInWithProvider(this, oAuthProviderBuilder.build())
            .addOnSuccessListener {
                Log.d(TAG, "success:" + it)
                val credential = it.credential as OAuthCredential;
                val username = it.additionalUserInfo?.username
                val accessToken = credential.accessToken
                val secret = credential.secret
                Log.d(TAG, "username:$username")
                Log.d(TAG, "profile.id:" + it.additionalUserInfo?.profile?.get("id"))
                Log.d(TAG, "accessToken:$accessToken")
                Log.d(TAG, "secret:$secret")
                Log.d(TAG, "user.uid:" + it.user?.uid)
                Log.d(TAG, "consumer_key:${OAuthConst.oauth_consumer_key}")
                Log.d(TAG, "consumerSecret:${OAuthConst.consumerSecret}")
                val userRequest = UserRequest(username ?: "", accessToken, secret ?: "")
                getListFriends(userRequest)
            }
            .addOnFailureListener {
                Log.d(TAG, "failure:" + it)
            }
    }

    private fun getListFriends(userRequest: UserRequest) {
        userRequest.callListFriends().enqueue(object : Callback<List<Long>> {
            override fun onFailure(call: Call<List<Long>>, t: Throwable) {
                Log.d(TAG, t?.message)
            }

            override fun onResponse(call: Call<List<Long>>, response: Response<List<Long>>) {
                Log.d(TAG, "response.code:${response.code()}")
                Log.d(TAG, "response.headers:${response.headers()}")
                response.body()?.forEach { Log.d(TAG, "id:$it") }
            }
        } )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

//data class Repo(val id: Long, val name: String)

interface TwitterApi {
    @GET("1.1/friends/ids.json")
    fun listFriends(@Query("cursor") cursor: Long): Call<List<Long>>
}

class UserRequest constructor(private val username: String,
                              val accessToken: String, val secret: String) {
    private val okHttpClient: OkHttpClient = OkHttpClient().newBuilder().addInterceptor { chain ->
        val request = chain.request()
        Log.d(TAG, "okHttpClient url=${request.url()}")
        val authorization = authorization(request,
            OAuthConst.oauth_consumer_key, OAuthConst.consumerSecret,
            accessToken, secret)
        val newRequest = request.newBuilder()
            .addHeader("Authorization", authorization)
            .build()
        Log.d(TAG, "newRequest.headers:${newRequest.headers()}")
        chain.proceed(newRequest)
    }.build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.twitter.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    private val twitterApi = retrofit.create(TwitterApi::class.java)

    fun callListFriends() : Call<List<Long>> {
        Log.d(TAG, "username=$username")
        return twitterApi.listFriends(-1)
    }

companion object {

    fun authorization(request: Request,
                      oauth_consumer_key: String, consumerSecret: String,
                      accessToken: String, secret: String): String {
        fun oauth_nonce(): String {
            return "${System.nanoTime()}${abs(SecureRandom().nextLong())}"
        }
        fun oauth_timestamp(): String {
            return "${System.currentTimeMillis()/1000}"
        }
        fun String.encode(): String {
            return URLEncoder.encode(this, "UTF-8")
//
//                .replace("*", "%2A")
//                .replace("+", "%20")
//                .replace("%7E", "~")
        }
        fun String.decode(): String {
            return URLDecoder.decode(this, "UTF-8")
        }
        fun oauthParams(): MutableMap<String, String?> {
            val params = mutableMapOf<String, String?>()
            params["oauth_consumer_key"] = oauth_consumer_key
            params["oauth_nonce"] = oauth_nonce()
            params["oauth_signature"] = null
            params["oauth_signature_method"] = "HMAC-SHA1"
            params["oauth_timestamp"] = oauth_timestamp()
            params["oauth_token"] = accessToken
            params["oauth_version"] = "1.0"
            return params
        }
        fun parameterString(): String {
            fun queryParams() : Map<String, String> {
                val params = mutableMapOf<String, String>()
                val httpUrl = request.url()
                for (index in 0 until httpUrl.querySize())
                    params[httpUrl.queryParameterName(index)] =
                        httpUrl.queryParameterValue(index)
                return params
            }
            fun bodyParams() : Map<String, String> {
                val params = mutableMapOf<String, String>()
                val body = request.body()
                if (body !is FormBody)
                    return params
                for (index in 0 until body.size())
                    params[body.encodedName(index)] = body.encodedValue(index)
                return params
            }
            val params = mutableMapOf<String, String>()
            params += queryParams()
            params += bodyParams()
            oauthParams().forEach { (name, value) ->
                value?.let {params[name.encode()] = value.encode()}
            }
            val sortedParams = params.toSortedMap()
            val result = sortedParams
                .map {(name, value) -> "$name=$value"}
                .joinToString(separator = "&")
            Log.d(TAG, "parameterString:$result")
            return result
        }
        fun baseUrl(): String {
            val uri = Uri.parse(request.url().toString())
            return "${uri.scheme}://${uri.host}${uri.path}"
        }
        fun signatureBaseString(): String {
            val result = request.method().toUpperCase() +
                    "&${baseUrl().encode()}" +
                    "&${parameterString().encode()}"
            Log.d(TAG, "signatureBaseString:$result")
            return result
        }
        fun signingKey(): String {
            val result = "${consumerSecret.encode()}&${secret.encode()}"
            Log.d(TAG, "signingKey:$result")
            return result
        }
        fun oauth_signature(): String {
            val signatureBaseBytes = signatureBaseString().toByteArray()
            val signingKeyBytes = signingKey().toByteArray()
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(signingKeyBytes, "HmacSHA1"))
            val signatureBytes = mac.doFinal(signatureBaseBytes)
            val result = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
            Log.d(TAG, "oauth_signature:$result")
            return result
        }
        fun headerString() : String {
            val params = oauthParams()
            params["oauth_signature"] = oauth_signature()
            val result = "OAuth " + params.map { (name, value) ->
                    value?.let { "${name.encode()}=\"${value.encode()}\"" }
                }.joinToString(", ")
            Log.d(TAG, "headerString:$result")
            return result
        }
        return headerString();
/*
      parameterString
       map(queryParams, bodyParams, oAuthSignatureParams)
          .percentEncode(Key and Value)
          .sortByKey()
          .reduce(key1=value1&key2=value2...)
      signatureBaseString
       method.upperCase()&percentEncode(baseUrl)&percentEncode(parameterString)
      signingKey
       percentEncode(consumerSecret)&percentEncode(tokenSecret)
      oauth_signature
       hash_hmac(signatureBaseString, signigKey).base64()
      header Authorization:
       headerString = "OAuth " +
             map(oAuthParams)
             .percentEncode(Key and Value)
             .reduce(key1="value1", key2="value2"...)
*/
/*
      Header example:
      Authorization:
      OAuth oauth_consumer_key="xvz1evFS4wEEPTGEFPHBog",
      oauth_nonce="kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg",
      oauth_signature="tnnArxj06cWHq44gCs1OSKk%2FjLY%3D",
      oauth_signature_method="HMAC-SHA1",
      oauth_timestamp="1318622958",
      oauth_token="370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
      oauth_version="1.0"
*/
    }

    private const val TAG = "UserRequest"
}

}