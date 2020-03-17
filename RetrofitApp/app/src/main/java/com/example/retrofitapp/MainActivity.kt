package com.example.retrofitapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class Repo(val id: Long, val name: String)

interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user : String) : Call<List<Repo>>
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val service = retrofit.create(GitHubService::class.java)

        val serviceListRepos = service.listRepos("lv19")

        serviceListRepos.enqueue(object : Callback<List<Repo>> {
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
