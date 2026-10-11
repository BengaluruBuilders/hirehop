package com.tailormyresume.feature.tailor.impl.export

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.ResumeSettingsRepository
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import java.io.File
import javax.inject.Inject
import kotlin.time.Clock

internal sealed interface ExportOutcome {
    data class Exported(
        val fileName: String,
        val file: File,
        val pageCount: Int,
        val sizeBytes: Long,
    ) : ExportOutcome

    data object Refused : ExportOutcome
}

internal class ExportResumeUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: ResumeSettingsRepository,
    private val exportHistory: ExportHistoryRepository,
    private val assembler: ResumeDocumentAssembler,
    private val renderer: ResumePdfRenderer,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String): ExportOutcome = TODO()
}
