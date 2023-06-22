package com.example.domain.repositories

interface Repository {
    suspend fun saveJson(json: String, fileName: String) : Result<Unit>
//    fun exportJson()
//    fun saveJson()
//    fun publish()
}