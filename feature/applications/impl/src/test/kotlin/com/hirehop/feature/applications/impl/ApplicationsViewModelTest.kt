package com.hirehop.feature.applications.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class ApplicationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private lateinit var viewModel: ApplicationsViewModel

    @Before
    fun setUp() {
        viewModel = ApplicationsViewModel(applicationRepository)
    }

    @Test
    fun uiState_beforeRepositoryEmits_isLoading() {
        assertThat(viewModel.uiState.value).isEqualTo(ApplicationsUiState.Loading)
    }

    @Test
    fun uiState_whenNoApplications_isEmpty() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(emptyList())

            assertThat(expectMostRecentItem()).isEqualTo(ApplicationsUiState.Empty)
        }
    }

    @Test
    fun uiState_whenApplicationsExist_sortsByUpdatedAtDescending() = runTest {
        val oldest = testApplication(id = "oldest", updatedAtEpochSeconds = 100)
        val newest = testApplication(id = "newest", updatedAtEpochSeconds = 300)
        val middle = testApplication(id = "middle", updatedAtEpochSeconds = 200)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(oldest, newest, middle))

            assertThat(expectMostRecentItem())
                .isEqualTo(ApplicationsUiState.Success(listOf(newest, middle, oldest)))
        }
    }

    @Test
    fun uiState_whenApplicationIsUpdated_movesItToTheTop() = runTest {
        val first = testApplication(id = "first", updatedAtEpochSeconds = 100)
        val second = testApplication(id = "second", updatedAtEpochSeconds = 200)
        val touchedFirst = first.copy(updatedAt = Instant.fromEpochSeconds(500))

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(first, second))
            assertThat(expectMostRecentItem().ids()).containsExactly("second", "first").inOrder()

            applicationRepository.sendApplications(listOf(touchedFirst, second))
            assertThat(expectMostRecentItem().ids()).containsExactly("first", "second").inOrder()
        }
    }

    @Test
    fun uiState_whenAllApplicationsAreRemoved_isEmpty() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("only")))
            applicationRepository.sendApplications(emptyList())

            assertThat(expectMostRecentItem()).isEqualTo(ApplicationsUiState.Empty)
        }
    }

    private fun ApplicationsUiState.ids(): List<String> =
        (this as ApplicationsUiState.Success).items.map { it.id }
}
