package com.example.tutionmanager

data class Material(

    val id: String = "",
    val title: String = "",
    val fileUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
