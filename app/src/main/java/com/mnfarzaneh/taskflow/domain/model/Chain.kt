package com.mnfarzaneh.taskflow.domain.model

data class Chain(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)