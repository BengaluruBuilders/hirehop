package com.hirehop.core.data.mock

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Test

class MockStateJsonTest {

    @Serializable
    private data class Sample(val name: String, val count: Int = 0)

    private val store = TestMockStateStore()

    @Test
    fun aWrittenValueIsReadBackAsTheSameObject() = runTest {
        store.writeValue("sample", Sample.serializer(), Sample("a", 2))

        assertThat(store.readValue("sample", Sample.serializer())).isEqualTo(Sample("a", 2))
    }

    @Test
    fun aMissingValueReadsAsNull() = runTest {
        assertThat(store.readValue("sample", Sample.serializer())).isNull()
    }

    @Test
    fun textThatIsNotTheShapeReadsAsNull() = runTest {
        store.write("sample", "not json")

        assertThat(store.readValue("sample", Sample.serializer())).isNull()
    }

    @Test
    fun unknownFieldsAreIgnored() = runTest {
        store.write("sample", """{"name":"a","extra":true}""")

        assertThat(store.readValue("sample", Sample.serializer())).isEqualTo(Sample("a"))
    }

    @Test
    fun observingFollowsEveryWrite() = runTest {
        store.observeValue("sample", Sample.serializer()).test {
            assertThat(awaitItem()).isNull()
            store.writeValue("sample", Sample.serializer(), Sample("b"))
            assertThat(awaitItem()).isEqualTo(Sample("b"))
        }
    }
}
