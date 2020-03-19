package com.example.retrofitapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userRequest = UserRequest("lv19")

        userRequest.callListRepos().enqueue(object : Callback<List<Repo>> {
            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                Log.d(TAG, t?.message)
            }
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                response.body()?.forEach { Log.d(TAG, "repo:$it") }
            }
        })

        userRequest.callListRepos().enqueue(object : Callback<List<Repo>> {
            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                Log.d(TAG, t?.message)
            }
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                response.body()?.forEach { Log.d(TAG, "repo:$it") }
            }
        })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

data class Repo(val id: Long, val name: String)

interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user : String) : Call<List<Repo>>
}

class UserRequest constructor(val name: String) {
    private val okHttpClient: OkHttpClient = OkHttpClient().newBuilder().addInterceptor { chain ->
        val request = chain.request()
        Log.d(TAG, "okHttpClient url=${request.url()}")
        chain.proceed(request)
    }.build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    private val service = retrofit.create(GitHubService::class.java)

    fun callListRepos() : Call<List<Repo>> {
        Log.d(TAG, "name=$name")
        return service.listRepos(name)
    }

    companion object {
        private const val TAG = "UserRequest"
    }
}