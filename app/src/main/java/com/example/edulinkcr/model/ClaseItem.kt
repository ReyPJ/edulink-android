package com.example.edulinkcr.model

import com.google.gson.annotations.SerializedName

data class ClaseItem(
    val center: String,
    val id: Int,
    val name: String,
    val students: List<Int>,
    @SerializedName("students_name")
    val studentsName: List<String>,
    val teacher: Int,
    @SerializedName("teacher_name")
    val teacherName: String,
    val subject: String
)

data class CreateClaseItem(
    val center: String,
    val name: String,
    val students: List<Int>,
    val teacher: Int,
    val subject: String
)

data class AddStudentsToClass(
    val students: List<Int>
)
