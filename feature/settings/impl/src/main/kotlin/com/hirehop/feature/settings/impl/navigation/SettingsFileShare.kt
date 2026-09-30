package com.hirehop.feature.settings.impl.navigation

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

class SettingsFileProvider : FileProvider()

@Singleton
internal class SettingsExportFileStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun write(fileName: String, writeContent: (OutputStream) -> Unit): File {
        val directory = File(context.cacheDir, EXPORT_DIRECTORY)
        directory.mkdirs()
        val target = File(directory, fileName)
        val temporary = File(directory, fileName + TEMPORARY_SUFFIX)
        try {
            temporary.outputStream().use(writeContent)
            if (!temporary.renameTo(target)) throw IOException("Could not move the export to ${target.name}")
        } finally {
            temporary.delete()
        }
        return target
    }

    fun fileFor(fileName: String): File? =
        File(File(context.cacheDir, EXPORT_DIRECTORY), fileName).takeIf { file -> file.isFile }

    private companion object {
        const val EXPORT_DIRECTORY = "data-exports"
        const val TEMPORARY_SUFFIX = ".tmp"
    }
}

@Singleton
internal class SettingsDataExportWriter @Inject constructor(
    private val store: SettingsExportFileStore,
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val paymentGateway: PaymentGateway,
) {
    suspend fun write(fileName: String): File {
        val profile = profileRepository.observeProfile().first()
        val applications = applicationRepository.observeApplications().first()
        val entitlement = runCatching { paymentGateway.entitlement() }.getOrNull()
        return store.write(fileName) { output ->
            ZipOutputStream(output).use { zip ->
                zip.writeEntry(PROFILE_ENTRY, profile?.asExportText().orEmpty().ifEmpty { NOTHING_HELD })
                zip.writeEntry(APPLICATIONS_ENTRY, applications.asExportText())
                zip.writeEntry(PURCHASES_ENTRY, entitlement?.asExportText() ?: NOTHING_HELD)
                zip.finish()
            }
        }
    }

    private fun ZipOutputStream.writeEntry(name: String, body: String) {
        putNextEntry(ZipEntry(name).apply { time = PINNED_TIME })
        write(body.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun CandidateProfile.asExportText(): String = buildString {
        appendLine(NAME_LABEL + fullName)
        appendLine(HEADLINE_LABEL + headline)
        appendLine(EMAIL_LABEL + email)
        appendLine(PHONE_LABEL + phone)
        appendLine()
        appendLine("$entries.size facts")
        entries.forEach { entry ->
            appendLine()
            appendLine("${entry.id} · ${entry.title} · ${entry.source} · confirmed=${entry.isConfirmed}")
            if (entry.organization.isNotBlank()) appendLine(entry.organization)
            if (entry.startDate.isNotBlank() || entry.endDate.isNotBlank()) {
                appendLine("${entry.startDate} - ${entry.endDate}")
            }
            entry.bullets.forEach { bullet -> appendLine("  - ${bullet.text}") }
        }
    }

    private fun List<JobApplication>.asExportText(): String =
        if (isEmpty()) {
            NOTHING_HELD
        } else {
            joinToString(separator = "\n\n") { application ->
                buildString {
                    appendLine("${application.job.title} · ${application.job.company}")
                    appendLine("Status: ${application.status}")
                    appendLine("Updated: ${application.updatedAt}")
                    appendLine("Notes: ${application.notes.ifBlank { NONE } }")
                    appendLine()
                    appendLine(application.job.rawText)
                    application.gapAnalysis?.let { analysis ->
                        appendLine()
                        appendLine("Gap analysis: ${analysis.keywordCoverage.covered} of ${analysis.keywordCoverage.total} key terms covered")
                        analysis.matches.forEach { match ->
                            appendLine("  - ${match.status}: ${match.requirement.text}")
                        }
                    }
                }
            }
        }

    private fun com.hirehop.core.domain.PurchaseEntitlement.asExportText(): String = buildString {
        appendLine("Free credits: $freeCredits")
        appendLine("Purchased credits: $purchasedCredits")
        appendLine("Pending packs: ${pendingPackIds.size}")
        appendLine()
        appendLine(NO_PAYMENT_NOTE)
    }

    private companion object {
        const val PROFILE_ENTRY = "profile.txt"
        const val APPLICATIONS_ENTRY = "applications.txt"
        const val PURCHASES_ENTRY = "purchases.txt"
        const val PINNED_TIME = 1_000_000_000_000L
        const val NOTHING_HELD = "HireHop holds nothing here."
        const val NONE = "none"
        const val NAME_LABEL = "Name: "
        const val HEADLINE_LABEL = "Headline: "
        const val EMAIL_LABEL = "Email: "
        const val PHONE_LABEL = "Phone: "
        const val NO_PAYMENT_NOTE =
            "This build of HireHop takes no payment, so there is no receipt, no order date, and no store account."
    }
}

internal fun createSettingsShareIntent(
    context: Context,
    file: File,
    subject: String,
    chooserTitle: String,
): Intent {
    val authority = "${context.packageName}.settings.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = ZIP_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, chooserTitle)
}

private const val ZIP_MIME_TYPE = "application/zip"
