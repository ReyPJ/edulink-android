package com.example.edulinkcr.model

data class User(
    val id: Int,
    val username: String,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val role: String,
    val phone: String?,
    val center: String,
    val unique_code: String,
    val date_joined: String,
)

data class CreateUserRequest(
    val username: String,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val role: String,
    val phone: String?,
    val center: String,
    val unique_code: String,
    val password: String,
)
