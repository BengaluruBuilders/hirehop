package com.hirehop.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers.IO
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.connectivity.MockConnectivityControl
import com.hirehop.core.data.connectivity.OfflineConnectivityMonitor
import com.hirehop.core.data.mock.DataStoreMockStateStore
import com.hirehop.core.data.mock.DelayMockLatency
import com.hirehop.core.data.mock.MockLatency
import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.repository.ApplicationCleanup
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.CoverLetterRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import com.hirehop.core.data.repository.OfflineFirstApplicationRepository
import com.hirehop.core.data.repository.OfflineFirstProfileRepository
import com.hirehop.core.data.repository.PrepPlanRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.data.repository.StoredApplicationCleanup
import com.hirehop.core.data.repository.StoredCoverLetterRepository
import com.hirehop.core.data.repository.StoredExportHistoryRepository
import com.hirehop.core.data.repository.StoredPrepPlanRepository
import com.hirehop.core.data.repository.StoredSessionRepository
import com.hirehop.core.data.repository.StoredTailoringReviewStateRepository
import com.hirehop.core.data.repository.TailoringReviewStateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsProfileRepository(
        profileRepository: OfflineFirstProfileRepository,
    ): ProfileRepository

    @Binds
    internal abstract fun bindsApplicationRepository(
        applicationRepository: OfflineFirstApplicationRepository,
    ): ApplicationRepository

    @Binds
    internal abstract fun bindsMockStateStore(store: DataStoreMockStateStore): MockStateStore

    @Binds
    internal abstract fun bindsSessionRepository(repository: StoredSessionRepository): SessionRepository

    @Binds
    internal abstract fun bindsExportHistoryRepository(
        repository: StoredExportHistoryRepository,
    ): ExportHistoryRepository

    @Binds
    internal abstract fun bindsPrepPlanRepository(repository: StoredPrepPlanRepository): PrepPlanRepository

    @Binds
    internal abstract fun bindsTailoringReviewStateRepository(
        repository: StoredTailoringReviewStateRepository,
    ): TailoringReviewStateRepository

    @Binds
    internal abstract fun bindsCoverLetterRepository(repository: StoredCoverLetterRepository): CoverLetterRepository

    @Binds
    internal abstract fun bindsApplicationCleanup(cleanup: StoredApplicationCleanup): ApplicationCleanup

    @Binds
    internal abstract fun bindsMockLatency(latency: DelayMockLatency): MockLatency

    @Binds
    internal abstract fun bindsConnectivityMonitor(monitor: OfflineConnectivityMonitor): ConnectivityMonitor

    @Binds
    internal abstract fun bindsMockConnectivityControl(monitor: OfflineConnectivityMonitor): MockConnectivityControl

    companion object {
        @Provides
        fun providesClock(): Clock = Clock.System

        @Provides
        @Singleton
        internal fun providesMockStateDataStore(
            @ApplicationContext context: Context,
            @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(ioDispatcher + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(MOCK_STATE_FILE) },
        )

        private const val MOCK_STATE_FILE = "mock_backend"
    }
}
