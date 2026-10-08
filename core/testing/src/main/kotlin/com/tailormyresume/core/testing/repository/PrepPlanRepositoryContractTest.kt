package com.tailormyresume.core.testing.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.PrepPlanRepository
import com.tailormyresume.core.model.PrepPlanItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class PrepPlanRepositoryContractTest {

    protected abstract fun createPrepPlanRepository(): PrepPlanRepository

    private val first = PrepPlanItem("q1", "Explain a Room migration")
    private val second = PrepPlanItem("q2", "Describe a coroutine bug you fixed")

    @Test
    fun aNewPlanIsEmpty() = runTest {
        assertThat(createPrepPlanRepository().observeItems("app-1").first()).isEmpty()
    }

    @Test
    fun addedItemsKeepTheirFieldsAndOrder() = runTest {
        val plan = createPrepPlanRepository()

        plan.add("app-1", first)
        plan.add("app-1", second)

        assertThat(plan.observeItems("app-1").first()).containsExactly(first, second).inOrder()
    }

    @Test
    fun anItemWithAKnownIdIsNotAddedTwice() = runTest {
        val plan = createPrepPlanRepository()

        plan.add("app-1", first)
        plan.add("app-1", first.copy(text = "Changed"))

        assertThat(plan.observeItems("app-1").first()).containsExactly(first)
    }

    @Test
    fun removeDeletesOnlyThatItem() = runTest {
        val plan = createPrepPlanRepository()
        plan.add("app-1", first)
        plan.add("app-1", second)

        plan.remove("app-1", "q1")

        assertThat(plan.observeItems("app-1").first()).containsExactly(second)
    }

    @Test
    fun setDoneMarksAndUnmarksOneItem() = runTest {
        val plan = createPrepPlanRepository()
        plan.add("app-1", first)
        plan.add("app-1", second)

        plan.setDone("app-1", "q2", true)
        assertThat(plan.observeItems("app-1").first().map { it.done }).containsExactly(false, true).inOrder()

        plan.setDone("app-1", "q2", false)
        assertThat(plan.observeItems("app-1").first().map { it.done }).containsExactly(false, false)
    }

    @Test
    fun setDoneOnAnUnknownItemChangesNothing() = runTest {
        val plan = createPrepPlanRepository()
        plan.add("app-1", first)

        plan.setDone("app-1", "missing", true)

        assertThat(plan.observeItems("app-1").first()).containsExactly(first)
    }

    @Test
    fun eachApplicationHasItsOwnPlan() = runTest {
        val plan = createPrepPlanRepository()
        plan.add("app-1", first)

        assertThat(plan.observeItems("app-2").first()).isEmpty()
    }

    @Test
    fun clearForRemovesOnlyThatApplication() = runTest {
        val plan = createPrepPlanRepository()
        plan.add("app-1", first)
        plan.add("app-2", second)

        plan.clearFor("app-1")

        assertThat(plan.observeItems("app-1").first()).isEmpty()
        assertThat(plan.observeItems("app-2").first()).containsExactly(second)
    }
}
