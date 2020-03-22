package lvlambda.twitter

import android.util.Log
import com.example.firebasetwitteroauth.OAuthConst
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TwitterApiBuilder constructor(private val username: String,
                                    val consumerKey: String, val consumerSecret: String,
                                    val accessToken: String, val secret: String) {
    private val okHttpClient: OkHttpClient = OkHttpClient().newBuilder().addInterceptor { chain ->
        val request = chain.request()
        Log.d(TAG, "okHttpClient url=${request.url()}")
        val authorization = TwitterOAuth.authorization(
            request,
            consumerKey, consumerSecret,
            accessToken, secret
        )
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

    fun <TService> create(service: Class<TService>): TService {
        return retrofit.create(service)
    }

    companion object {
        private const val TAG = "TwitterApiBuilder"
    }
}