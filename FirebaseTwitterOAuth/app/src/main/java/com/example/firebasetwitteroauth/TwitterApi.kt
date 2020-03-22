package com.example.firebasetwitteroauth

import retrofit2.Call
import retrofit2.http.*

data class TwitterUsersIds(
    val ids: List<Long>,
    val next_cursor: Long,
    val previous_cursor: Long
)
data class TwitterUser(
    val id: Long,
    val screen_name: String
)

typealias ListTwitterUser = List<TwitterUser>


interface TwitterApi {
    @GET("1.1/friends/ids.json")
    fun friendsIds(@Query("cursor") cursor: Long = -1): Call<TwitterUsersIds>

    @GET("1.1/followers/ids.json")
    fun followersIds(@Query("cursor") cursor: Long = -1): Call<TwitterUsersIds>

    @GET("1.1/users/lookup.json")
    //user_id: comma separated list of user IDs -use a POST for larger requests-
    fun usersLookupGet(@Query("user_id") user_id: String): Call<ListTwitterUser>

    @FormUrlEncoded
    @POST("1.1/users/lookup.json")
    //user_id: comma separated list of user IDs -up to 100-
    fun usersLookup(@Field("user_id") user_id: String): Call<ListTwitterUser>
    companion object {
        const val MAX_LOOKUP_IDS = 100
    }
}
