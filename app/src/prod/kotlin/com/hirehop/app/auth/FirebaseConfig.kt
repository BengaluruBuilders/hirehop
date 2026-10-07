package com.hirehop.app.auth

data class FirebaseConfig(
    val apiKey: String,
    val appId: String,
    val projectId: String,
    val webClientId: String,
) {
    val isComplete: Boolean
        get() = listOf(apiKey, appId, projectId, webClientId).none { it.isBlank() }
}
