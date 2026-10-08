package com.tailormyresume.core.data.mock

enum class MockOperation { SIGN_IN, LOAD_PACKS, PURCHASE, RESTORE, EXPORT_DATA, DELETE_ACCOUNT_STEP }

interface MockLatency {
    suspend fun await(operation: MockOperation)
}

object NoMockLatency : MockLatency {
    override suspend fun await(operation: MockOperation) = Unit
}
