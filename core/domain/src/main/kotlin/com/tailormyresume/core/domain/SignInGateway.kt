package com.tailormyresume.core.domain

interface SignInGateway {
    suspend fun currentAccount(): SignInAccount?

    suspend fun signIn(): SignInResult

    suspend fun signOut()
}
