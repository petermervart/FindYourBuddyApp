package com.example.testing

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @GET("/games/")
    fun getGames(): Call<GamesResult>

    @GET("/friend_requests/")
    fun getFriendRequests(@Query("user_id") id: Int): Call<FriendRequestResult>

    @GET("/users/")
    fun getProfile(@Query("user_id") id: Int): Call<ProfileResult>

    @PUT("/users/{id}/")
    fun putProfile(@Path("id") id: Int, @Body requestBody: RequestBody): Call<Void>

    @GET("/messages/")
    fun getConversations(@Query("user_id") id: Int): Call<ConversationResult>

    @GET("/messages/")
    fun getMessages(@Query("user_1") id1: Int, @Query("user_2") id2: Int): Call<MessageResult>

    @POST("/messages/")
    fun postMessage(@Body requestBody: RequestBody): Call<Void>

    @GET("/friends/")
    fun getFriends(@Query("user_id") id: Int): Call<FriendResult>

    @DELETE("/friends/")
    fun deleteFriends(@Query("user_1") id1: Int, @Query("user_2") id2: Int): Call<Void>

    @POST("/friends/")
    fun postFriends(@Body requestBody: RequestBody): Call<Void>

    @DELETE("/friend_requests/")
    fun deleteFriendRequests(@Query("sender_id") id1: Int, @Query("reciever_id") id2: Int): Call<Void>

    @POST("/friend_requests/")
    fun postFriendRequests(@Body requestBody: RequestBody): Call<Void>

    @GET("/login/")
    fun getLogin(): Call<LoginResult>

    @POST("/register/")
    fun postRegister(@Body requestBody: RequestBody): Call<Void>

    @GET("/statuses/")
    fun getStatuses(@Query("owner") id: Int) : Call<StatusesResult>

    @POST("/statuses/")
    fun postStatuses(@Body requestBody: RequestBody) : Call<Void>

    @DELETE("/statuses/")
    fun deleteStatus(@Query("status_id") id: Int) : Call<Void>

    @GET("/advertisements/")
    fun getAdNoFilter(@Query("user_id") id: Int) : Call<AdvertisementResult>

    @DELETE("/advertisements/")
    fun deleteAd(@Query("ad_id") id: Int) : Call<Void>

    @GET("/ranks/")
    fun getRanks(@Query("game_id") id: Int) : Call<RankResult>

    @POST("/advertisements/")
    fun postAdvertisement(@Body requestBody: RequestBody) : Call<Void>

    @GET("/advertisements/")
    fun getAdFilter(@Query("user_id") id1: Int, @Query("game") id2: Int, @Query("rank") id3: Int) : Call<AdvertisementResult>
}