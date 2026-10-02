package com.hirehop.core.domain.account

import javax.inject.Inject

class ExportAccountDataUseCase @Inject constructor(
    private val collectAccountData: CollectAccountDataUseCase,
    private val exporter: AccountDataExporter,
) {
    suspend operator fun invoke(): AccountDataArchive = exporter.export(collectAccountData())
}
