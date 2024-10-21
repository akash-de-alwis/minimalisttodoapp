package com.example.minimalisttodoapp.models

import java.io.Serializable

data class Task(
    val name: String,
    val description: String,
    val time: String,
    val priority: Int,
    val date: String
) : Serializable