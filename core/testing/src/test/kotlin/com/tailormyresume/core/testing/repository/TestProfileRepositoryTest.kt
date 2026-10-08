package com.tailormyresume.core.testing.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.testing.data.sampleProfile
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestProfileRepositoryTest {

    private val repository = TestProfileRepository()

    @Test
    fun observeProfile_emitsNull_beforeAnyProfileIsSaved() = runTest {
        repository.observeProfile().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveProfile_emitsSavedProfile() = runTest {
        repository.observeProfile().test {
            assertThat(awaitItem()).isNull()

            repository.saveProfile(sampleProfile)

            assertThat(awaitItem()).isEqualTo(sampleProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearProfile_emitsNull_afterProfileWasSaved() = runTest {
        repository.saveProfile(sampleProfile)

        repository.observeProfile().test {
            assertThat(awaitItem()).isEqualTo(sampleProfile)

            repository.clearProfile()

            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun sendProfile_emitsGivenProfile() = runTest {
        repository.observeProfile().test {
            assertThat(awaitItem()).isNull()

            repository.sendProfile(sampleProfile)

            assertThat(awaitItem()).isEqualTo(sampleProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
