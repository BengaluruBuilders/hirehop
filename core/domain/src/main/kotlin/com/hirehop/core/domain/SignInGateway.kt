package com.hirehop.core.domain

interface SignInGateway {
    suspend fun currentAccount(): SignInAccount?

    suspend fun signIn(): SignInResult

    suspend fun signOut()
}
