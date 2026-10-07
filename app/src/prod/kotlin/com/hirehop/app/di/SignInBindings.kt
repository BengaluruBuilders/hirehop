package com.hirehop.app.di

import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.offline.OfflineSignInGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SignInBindings {
    @Binds
    fun bindSignInGateway(impl: OfflineSignInGateway): SignInGateway
}
