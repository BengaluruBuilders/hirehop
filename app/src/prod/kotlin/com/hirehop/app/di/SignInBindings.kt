package com.hirehop.app.di

import com.hirehop.app.BuildConfig
import com.hirehop.app.auth.CredentialManagerGoogleSource
import com.hirehop.app.auth.FirebaseAuthClient
import com.hirehop.app.auth.FirebaseConfig
import com.hirehop.app.auth.FirebaseSessionClient
import com.hirehop.app.auth.ForegroundActivity
import com.hirehop.app.auth.ForegroundActivityTracker
import com.hirehop.app.auth.GoogleCredentialSource
import com.hirehop.app.auth.RemoteConsentUploader
import com.hirehop.app.auth.RemoteServerAccountDeleter
import com.hirehop.app.auth.RemoteSignInGateway
import com.hirehop.core.domain.ConsentUploader
import com.hirehop.core.domain.FirebaseUidProvider
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.account.ServerAccountDeleter
import com.hirehop.core.network.IdTokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SignInBindings {
    @Binds
    fun bindSignInGateway(impl: RemoteSignInGateway): SignInGateway

    @Binds
    fun bindGoogleCredentialSource(impl: CredentialManagerGoogleSource): GoogleCredentialSource

    @Binds
    fun bindFirebaseSessionClient(impl: FirebaseAuthClient): FirebaseSessionClient

    @Binds
    fun bindIdTokenProvider(impl: FirebaseAuthClient): IdTokenProvider

    @Binds
    fun bindFirebaseUidProvider(impl: FirebaseAuthClient): FirebaseUidProvider

    @Binds
    fun bindConsentUploader(impl: RemoteConsentUploader): ConsentUploader

    @Binds
    fun bindServerAccountDeleter(impl: RemoteServerAccountDeleter): ServerAccountDeleter

    companion object {
        @Provides
        fun foregroundActivity(): ForegroundActivity = ForegroundActivityTracker

        @Provides
        fun firebaseConfig(): FirebaseConfig = FirebaseConfig(
            apiKey = BuildConfig.HIREHOP_FIREBASE_API_KEY,
            appId = BuildConfig.HIREHOP_FIREBASE_APP_ID,
            projectId = BuildConfig.HIREHOP_FIREBASE_PROJECT_ID,
            webClientId = BuildConfig.HIREHOP_WEB_CLIENT_ID,
        )
    }
}
