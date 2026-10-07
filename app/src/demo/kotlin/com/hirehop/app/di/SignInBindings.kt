package com.hirehop.app.di

import com.hirehop.core.domain.ConsentUploader
import com.hirehop.core.domain.FirebaseUidProvider
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.account.ServerAccountDeleter
import com.hirehop.core.domain.offline.OfflineConsentUploader
import com.hirehop.core.domain.offline.OfflineFirebaseUidProvider
import com.hirehop.core.domain.offline.OfflineServerAccountDeleter
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

    @Binds
    fun bindConsentUploader(impl: OfflineConsentUploader): ConsentUploader

    @Binds
    fun bindServerAccountDeleter(impl: OfflineServerAccountDeleter): ServerAccountDeleter

    @Binds
    fun bindFirebaseUidProvider(impl: OfflineFirebaseUidProvider): FirebaseUidProvider
}
