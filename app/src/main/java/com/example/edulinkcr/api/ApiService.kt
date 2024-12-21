package com.example.edulinkcr.api

import com.example.edulinkcr.model.CreateUserRequest
import com.example.edulinkcr.model.User
import com.example.edulinkcr.model.LoginRequest
import com.example.edulinkcr.model.LoginResponse
import com.example.edulinkcr.model.Clase
import com.example.edulinkcr.model.createClaseItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path

interface ApiService {
    // Users
    @GET("users/")
    fun getUsers(): Call<List<User>>
    @GET("users/{id}/")
    fun getUserDetails(@Path("id") id: Int): Call<User>
    @POST("users/")
    fun createUser(@Body user: CreateUserRequest): Call<CreateUserRequest>
    // Login
    @POST("token/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    // Classes
    @GET("classes/")
    fun getClasses(): Call<Clase>
    @POST("classes/")
    fun createClass(@Body clase: createClaseItem): Call<createClaseItem>
}