package com.hirehop.core.model

data class SignInAccount(
    val id: String,
    val displayName: String,
    val email: String,
) {
    companion object {
        val localAccount = SignInAccount(
            id = "local.account.1",
            displayName = "Priya Deshmukh",
            email = "priya.d@example.com",
        )
    }
}
