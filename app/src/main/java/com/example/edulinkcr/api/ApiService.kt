package com.example.edulinkcr.api

import com.example.edulinkcr.model.User
import com.example.edulinkcr.model.LoginRequest
import com.example.edulinkcr.model.LoginResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @GET("users/")
    fun getUsers(): Call<List<User>>
    @POST("token/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}