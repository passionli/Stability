package com.example.stability.core.extensions

fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    return emailRegex.matches(this)
}

fun String.isValidPassword(): Boolean {
    return this.length >= 6
}

fun String.isEmptyOrBlank(): Boolean {
    return this.isEmpty() || this.isBlank()
}
