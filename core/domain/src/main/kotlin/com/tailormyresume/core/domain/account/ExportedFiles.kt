package com.tailormyresume.core.domain.account

fun interface ExportedFiles {
    suspend fun deleteAll()

    companion object {
        val None = ExportedFiles { }
    }
}
