package com.tailormyresume.app.auth

import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.account.AccountWipeFinisher
import javax.inject.Inject

class RemoteAccountWipeFinisher @Inject constructor(
    private val signInGateway: SignInGateway,
    private val wiper: LocalDataWiper,
) : AccountWipeFinisher {
    override suspend fun finish() {
        signInGateway.signOut()
        wiper.wipeAll()
    }
}
