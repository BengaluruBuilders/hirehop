package com.tailormyresume.core.testing.account

import com.tailormyresume.core.domain.account.AccountData
import com.tailormyresume.core.domain.account.AccountDataArchive
import com.tailormyresume.core.domain.account.AccountDataExporter
import java.io.File

class TestAccountDataExporter : AccountDataExporter {

    val exported = mutableListOf<AccountData>()

    override suspend fun export(data: AccountData): AccountDataArchive {
        exported += data
        val file = File.createTempFile("test-account-data", ".zip").apply { deleteOnExit() }
        return AccountDataArchive(fileName = file.name, file = file)
    }
}
