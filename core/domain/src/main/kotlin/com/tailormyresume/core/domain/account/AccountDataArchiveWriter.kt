package com.tailormyresume.core.domain.account

import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AccountDataArchiveWriter {

    fun write(data: AccountData, target: File, extraEntries: Map<String, String> = emptyMap()) {
        target.parentFile?.mkdirs()
        val temporary = File(target.parentFile, target.name + TEMPORARY_SUFFIX)
        try {
            ZipOutputStream(temporary.outputStream()).use { zip ->
                zip.writeEntry(ACCOUNT_ENTRY, accountText(data))
                zip.writeEntry(PROFILE_ENTRY, profileText(data))
                zip.writeEntry(APPLICATIONS_ENTRY, applicationsText(data))
                zip.writeEntry(PURCHASES_ENTRY, purchasesText(data))
                extraEntries.forEach { (name, body) -> zip.writeEntry(name, body) }
            }
            if (!temporary.renameTo(target)) throw IOException("Could not move the archive to ${target.name}")
        } finally {
            temporary.delete()
        }
    }

    private fun ZipOutputStream.writeEntry(name: String, body: String) {
        putNextEntry(ZipEntry(name).apply { time = PINNED_TIME })
        write(body.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun accountText(data: AccountData): String = buildString {
        appendLine("generatedAt=${data.generatedAt}")
        appendLine("account=${data.account?.email.orEmpty()}")
        appendLine("consentPurposes=${data.consent?.purposes?.joinToString(",").orEmpty()}")
        appendLine("consentAcceptedAt=${data.consent?.acceptedAt?.toString().orEmpty()}")
        appendLine("consentNoticeVersion=${data.consent?.noticeVersion.orEmpty()}")
    }

    private fun profileText(data: AccountData): String = buildString {
        val profile = data.profile ?: return@buildString
        appendLine("${profile.fullName} | ${profile.headline} | ${profile.email} | ${profile.phone}")
        appendLine("skills=${profile.skills.joinToString(",")}")
        profile.entries.forEach { entry ->
            appendLine("${entry.id} | ${entry.category} | ${entry.title} | ${entry.organization} | confirmed=${entry.isConfirmed}")
            entry.bullets.forEach { bullet -> appendLine("  ${bullet.id}: ${bullet.text}") }
        }
    }

    private fun applicationsText(data: AccountData): String = buildString {
        data.applications.forEach { application ->
            appendLine("${application.id} | ${application.job.title} | ${application.job.company} | ${application.status}")
            val exports = data.exports.filter { it.applicationId == application.id }
            exports.forEach { export -> appendLine("  export ${export.format} ${export.fileName} ${export.exportedAt}") }
        }
    }

    private fun purchasesText(data: AccountData): String = buildString {
        appendLine("freeCredits=${data.entitlement.freeCredits}")
        appendLine("purchasedCredits=${data.entitlement.purchasedCredits}")
        data.purchases.forEach { purchase ->
            appendLine("${purchase.orderId} | ${purchase.packId} | ${purchase.state} | ${purchase.purchasedAt}")
        }
    }

    private companion object {
        const val ACCOUNT_ENTRY = "account.txt"
        const val PROFILE_ENTRY = "profile.txt"
        const val APPLICATIONS_ENTRY = "applications.txt"
        const val PURCHASES_ENTRY = "purchases.txt"
        const val TEMPORARY_SUFFIX = ".tmp"
        const val PINNED_TIME = 1_000_000_000_000L
    }
}
