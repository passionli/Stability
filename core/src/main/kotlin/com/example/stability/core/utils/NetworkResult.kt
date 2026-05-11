package com.example.stability.core.utils

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    object Timeout : NetworkResult<Nothing>
    object NetworkError : NetworkResult<Nothing>
}
