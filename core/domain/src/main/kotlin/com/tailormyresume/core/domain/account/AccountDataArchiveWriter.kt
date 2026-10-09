package com.tailormyresume.core.domain.account

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.JobRequirement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
                zip.writeEntry(MY_DATA_ENTRY, myDataJson(data))
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
            appendLine("  jobDescription: ${application.job.rawText}")
            application.job.requirements.forEach { appendLine("  requirement ${it.priority} ${it.type}: ${it.text}") }
            application.gapAnalysis?.let { analysis ->
                analysis.matches.forEach { appendLine("  analysis ${it.status}: ${it.requirement.text}") }
                appendLine("  keywordCoverage=${analysis.keywordCoverage.covered}/${analysis.keywordCoverage.total}")
            }
            application.tailoredResume?.bullets?.forEach { bullet ->
                appendLine("  resume ${bullet.decision}: ${bullet.proposedText} (was: ${bullet.originalText})")
            }
            if (application.notes.isNotBlank()) appendLine("  notes: ${application.notes}")
            data.coverLetters[application.id]?.paragraphs?.forEach { appendLine("  coverLetter: ${it.text}") }
            data.prepPlans[application.id].orEmpty().forEach { appendLine("  prep done=${it.done}: ${it.text}") }
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

    private fun myDataJson(data: AccountData): String = prettyJson.encodeToString(
        JsonObject.serializer(),
        buildJsonObject {
            put("generatedAt", data.generatedAt.toString())
            data.account?.let { account ->
                put(
                    "account",
                    buildJsonObject {
                        put("email", account.email)
                        put("displayName", account.displayName)
                    },
                )
            }
            data.consent?.let { consent ->
                put(
                    "consent",
                    buildJsonObject {
                        put("purposes", strings(consent.purposes.map { it.name }))
                        put("acceptedAt", consent.acceptedAt.toString())
                        put("noticeVersion", consent.noticeVersion)
                    },
                )
            }
            data.profile?.let { put("profile", profileJson(it)) }
            put(
                "entitlement",
                buildJsonObject {
                    put("freeCredits", data.entitlement.freeCredits)
                    put("purchasedCredits", data.entitlement.purchasedCredits)
                },
            )
            put(
                "purchases",
                buildJsonArray {
                    data.purchases.forEach { purchase ->
                        add(
                            buildJsonObject {
                                put("packId", purchase.packId)
                                put("orderId", purchase.orderId)
                                put("state", purchase.state.name)
                                put("purchasedAt", purchase.purchasedAt.toString())
                            },
                        )
                    }
                },
            )
            put("applications", buildJsonArray { data.applications.forEach { add(applicationJson(it, data)) } })
        },
    )

    private fun profileJson(profile: CandidateProfile): JsonObject = buildJsonObject {
        put("fullName", profile.fullName)
        put("email", profile.email)
        put("phone", profile.phone)
        put("headline", profile.headline)
        put("skills", strings(profile.skills))
        put(
            "entries",
            buildJsonArray {
                profile.entries.forEach { entry ->
                    add(
                        buildJsonObject {
                            put("id", entry.id)
                            put("category", entry.category.name)
                            put("title", entry.title)
                            put("organization", entry.organization)
                            put("startDate", entry.startDate)
                            put("endDate", entry.endDate)
                            put("source", entry.source.name)
                            put("isConfirmed", entry.isConfirmed)
                            put(
                                "bullets",
                                buildJsonArray {
                                    entry.bullets.forEach { bullet ->
                                        add(
                                            buildJsonObject {
                                                put("id", bullet.id)
                                                put("text", bullet.text)
                                            },
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            },
        )
    }

    private fun applicationJson(application: JobApplication, data: AccountData): JsonObject = buildJsonObject {
        put("id", application.id)
        put("role", application.job.title)
        put("company", application.job.company)
        put("status", application.status.name)
        put("notes", application.notes)
        put("createdAt", application.createdAt.toString())
        put("updatedAt", application.updatedAt.toString())
        put(
            "job",
            buildJsonObject {
                put("rawText", application.job.rawText)
                put(
                    "requirements",
                    buildJsonArray {
                        application.job.requirements.forEach { add(requirementJson(it)) }
                    },
                )
            },
        )
        application.gapAnalysis?.let { analysis ->
            put(
                "gapAnalysis",
                buildJsonObject {
                    put(
                        "matches",
                        buildJsonArray {
                            analysis.matches.forEach { match ->
                                add(
                                    buildJsonObject {
                                        put("requirementId", match.requirement.id)
                                        put("status", match.status.name)
                                        put("evidenceIds", strings(match.evidenceIds))
                                    },
                                )
                            }
                        },
                    )
                    put(
                        "keywordCoverage",
                        buildJsonObject {
                            put("covered", analysis.keywordCoverage.covered)
                            put("total", analysis.keywordCoverage.total)
                        },
                    )
                },
            )
        }
        application.tailoredResume?.let { resume ->
            put(
                "tailoredResume",
                buildJsonObject {
                    put(
                        "bullets",
                        buildJsonArray {
                            resume.bullets.forEach { bullet ->
                                add(
                                    buildJsonObject {
                                        put("id", bullet.id)
                                        put("entryId", bullet.entryId)
                                        put("original", bullet.originalText)
                                        put("proposed", bullet.proposedText)
                                        put("sourceIds", strings(bullet.sourceIds))
                                        put("decision", bullet.decision.name)
                                    },
                                )
                            }
                        },
                    )
                },
            )
        }
        data.coverLetters[application.id]?.let { letter ->
            put(
                "coverLetter",
                buildJsonObject {
                    put("writtenAt", letter.writtenAt.toString())
                    put(
                        "paragraphs",
                        buildJsonArray {
                            letter.paragraphs.forEach { paragraph ->
                                add(
                                    buildJsonObject {
                                        put("text", paragraph.text)
                                        put("isGreeting", paragraph.isGreeting)
                                        put("isUserEdited", paragraph.isUserEdited)
                                    },
                                )
                            }
                        },
                    )
                },
            )
        }
        put(
            "prepPlan",
            buildJsonArray {
                data.prepPlans[application.id].orEmpty().forEach { item ->
                    add(
                        buildJsonObject {
                            put("id", item.id)
                            put("text", item.text)
                            put("done", item.done)
                        },
                    )
                }
            },
        )
        put(
            "exports",
            buildJsonArray {
                data.exports.filter { it.applicationId == application.id }.forEach { export ->
                    add(
                        buildJsonObject {
                            put("format", export.format.name)
                            put("fileName", export.fileName)
                            put("exportedAt", export.exportedAt.toString())
                        },
                    )
                }
            },
        )
    }

    private fun requirementJson(requirement: JobRequirement): JsonObject = buildJsonObject {
        put("id", requirement.id)
        put("text", requirement.text)
        put("type", requirement.type.name)
        put("priority", requirement.priority.name)
        put("keywords", strings(requirement.keywords))
    }

    private fun strings(values: List<String>): JsonArray = buildJsonArray { values.forEach { add(it) } }

    private companion object {
        const val ACCOUNT_ENTRY = "account.txt"
        const val PROFILE_ENTRY = "profile.txt"
        const val APPLICATIONS_ENTRY = "applications.txt"
        const val PURCHASES_ENTRY = "purchases.txt"
        const val MY_DATA_ENTRY = "my-data.json"
        val prettyJson = Json { prettyPrint = true }
        const val TEMPORARY_SUFFIX = ".tmp"
        const val PINNED_TIME = 1_000_000_000_000L
    }
}
