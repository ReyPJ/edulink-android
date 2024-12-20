package com.example.edulinkcr.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access: String
)
