package com.example.domain.repositories

interface Repository {
    suspend fun saveJson(json: String) : Result<Unit>
//    fun exportJson()
//    fun saveJson()
//    fun publish()
}