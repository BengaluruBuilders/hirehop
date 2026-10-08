package com.tailormyresume.core.testing.mock

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.MockStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class MockStateStoreContractTest {

    protected abstract fun createStore(scope: CoroutineScope): MockStateStore

    @Test
    fun aMissingKeyReadsAsNull() = runTest {
        val store = createStore(backgroundScope)

        assertThat(store.read("missing")).isNull()
    }

    @Test
    fun aWrittenValueIsReadBack() = runTest {
        val store = createStore(backgroundScope)

        store.write("key", "value")

        assertThat(store.read("key")).isEqualTo("value")
    }

    @Test
    fun aSecondWriteReplacesTheFirst() = runTest {
        val store = createStore(backgroundScope)
        store.write("key", "one")

        store.write("key", "two")

        assertThat(store.read("key")).isEqualTo("two")
    }

    @Test
    fun removeDeletesOnlyThatKey() = runTest {
        val store = createStore(backgroundScope)
        store.write("a", "1")
        store.write("b", "2")

        store.remove("a")

        assertThat(store.read("a")).isNull()
        assertThat(store.read("b")).isEqualTo("2")
    }

    @Test
    fun removeWithPrefixDeletesOnlyMatchingKeys() = runTest {
        val store = createStore(backgroundScope)
        store.write("p.a", "1")
        store.write("p.b", "2")
        store.write("q.a", "3")

        store.removeWithPrefix("p.")

        assertThat(store.read("p.a")).isNull()
        assertThat(store.read("p.b")).isNull()
        assertThat(store.read("q.a")).isEqualTo("3")
    }

    @Test
    fun clearDeletesEveryKey() = runTest {
        val store = createStore(backgroundScope)
        store.write("a", "1")
        store.write("b", "2")

        store.clear()

        assertThat(store.read("a")).isNull()
        assertThat(store.read("b")).isNull()
    }

    @Test
    fun observeEmitsTheCurrentValueAndEveryChange() = runTest {
        val store = createStore(backgroundScope)
        store.write("key", "one")

        store.observe("key").test {
            assertThat(awaitItem()).isEqualTo("one")
            store.write("key", "two")
            assertThat(awaitItem()).isEqualTo("two")
            store.remove("key")
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun observeSkipsWritesToOtherKeys() = runTest {
        val store = createStore(backgroundScope)

        store.observe("key").test {
            assertThat(awaitItem()).isNull()
            store.write("other", "x")
            store.write("key", "y")
            assertThat(awaitItem()).isEqualTo("y")
        }
    }
}
