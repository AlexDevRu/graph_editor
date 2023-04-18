package com.example.mobilepaint.events

data class EventSave(
    val id: String,
    val oldFileName: String?,
    val newFileName: String,
    val published: Boolean
)
