package com.tailormyresume.core.domain.account

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.PurchaseEntitlement
import com.tailormyresume.core.domain.PurchaseRecord
import com.tailormyresume.core.domain.PurchaseState
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.EditType
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.core.model.PrepPlanItem
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.model.TailoredBullet
import com.tailormyresume.core.model.TailoredResume
import com.tailormyresume.core.model.WrittenCoverLetter
import com.tailormyresume.core.model.WrittenParagraph
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.sampleApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile
import kotlin.time.Instant

class AccountDataArchiveWriterTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val now = Instant.fromEpochMilliseconds(1_790_000_000_000)

    private val application = sampleApplication.copy(
        notes = "Call the recruiter on Tuesday",
        tailoredResume = TailoredResume(
            bullets = listOf(
                TailoredBullet(
                    id = "tailored-1",
                    entryId = "entry-1",
                    originalText = "Built a Kotlin app",
                    proposedText = "Built a Kotlin Android app",
                    sourceIds = listOf("bullet-project-1"),
                    editTypes = listOf(EditType.REWORD),
                    keywordsUsed = listOf("kotlin"),
                    violations = emptyList(),
                    decision = BulletDecision.ACCEPTED,
                ),
            ),
        ),
    )

    private val data = AccountData(
        generatedAt = now,
        account = SignInAccount.localAccount,
        consent = ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), now, ConsentRecord.CURRENT_NOTICE_VERSION),
        profile = canonicalCandidateProfile,
        applications = listOf(application),
        entitlement = PurchaseEntitlement(freeCredits = 1, purchasedCredits = 4, pendingPackIds = emptyList()),
        purchases = listOf(PurchaseRecord("application_pack_5", "order-1", now, PurchaseState.COMPLETED)),
        exports = listOf(ExportRecord(application.id, ExportFormat.PDF, "resume.pdf", now, CreditKind.FREE)),
        coverLetters = mapOf(application.id to WrittenCoverLetter(listOf(WrittenParagraph("Dear Example Corp team")), now)),
        prepPlans = mapOf(application.id to listOf(PrepPlanItem("prep-1", "Revise coroutines", done = true))),
    )

    private fun archive(extra: Map<String, String> = emptyMap()): Map<String, String> {
        val target = File(folder.root, "archive.zip")
        AccountDataArchiveWriter().write(data, target, extra)
        return ZipFile(target).use { zip ->
            zip.entries().asSequence().associate { it.name to zip.getInputStream(it).readBytes().decodeToString() }
        }
    }

    private fun myData(): JsonObject =
        Json.parseToJsonElement(archive().getValue("my-data.json")).jsonObject

    private fun JsonObject.array(name: String): JsonArray = getValue(name).jsonArray

    @Test
    fun myDataJsonHoldsEveryApplicationField() {
        val entry = myData().array("applications").single().jsonObject

        assertThat(entry.getValue("id").jsonPrimitive.content).isEqualTo(application.id)
        assertThat(entry.getValue("role").jsonPrimitive.content).isEqualTo(application.job.title)
        assertThat(entry.getValue("company").jsonPrimitive.content).isEqualTo(application.job.company)
        assertThat(entry.getValue("status").jsonPrimitive.content).isEqualTo("SAVED")
        assertThat(entry.getValue("notes").jsonPrimitive.content).isEqualTo("Call the recruiter on Tuesday")
        assertThat(entry.getValue("createdAt").jsonPrimitive.content).isEqualTo(application.createdAt.toString())
        assertThat(entry.getValue("updatedAt").jsonPrimitive.content).isEqualTo(application.updatedAt.toString())

        val job = entry.getValue("job").jsonObject
        assertThat(job.getValue("rawText").jsonPrimitive.content).isEqualTo(application.job.rawText)
        val requirement = job.array("requirements").first().jsonObject
        assertThat(requirement.getValue("type").jsonPrimitive.content).isEqualTo("SKILL")
        assertThat(requirement.getValue("priority").jsonPrimitive.content).isEqualTo("MUST_HAVE")
        assertThat(requirement.array("keywords").map { it.jsonPrimitive.content }).containsExactly("kotlin")

        val analysis = entry.getValue("gapAnalysis").jsonObject
        val match = analysis.array("matches").first().jsonObject
        assertThat(match.getValue("status").jsonPrimitive.content).isEqualTo("MET")
        assertThat(match.array("evidenceIds").map { it.jsonPrimitive.content }).containsExactly("bullet-project-1")
        assertThat(analysis.getValue("keywordCoverage").jsonObject.getValue("covered").jsonPrimitive.int).isEqualTo(1)

        val bullet = entry.getValue("tailoredResume").jsonObject.array("bullets").single().jsonObject
        assertThat(bullet.getValue("original").jsonPrimitive.content).isEqualTo("Built a Kotlin app")
        assertThat(bullet.getValue("proposed").jsonPrimitive.content).isEqualTo("Built a Kotlin Android app")
        assertThat(bullet.getValue("decision").jsonPrimitive.content).isEqualTo("ACCEPTED")
        assertThat(bullet.array("sourceIds").map { it.jsonPrimitive.content }).containsExactly("bullet-project-1")

        val paragraph = entry.getValue("coverLetter").jsonObject.array("paragraphs").single().jsonObject
        assertThat(paragraph.getValue("text").jsonPrimitive.content).isEqualTo("Dear Example Corp team")
        val prep = entry.array("prepPlan").single().jsonObject
        assertThat(prep.getValue("done").jsonPrimitive.boolean).isTrue()
        assertThat(entry.array("exports").single().jsonObject.getValue("fileName").jsonPrimitive.content).isEqualTo("resume.pdf")
    }

    @Test
    fun myDataJsonHoldsAccountConsentProfileAndPurchases() {
        val root = myData()

        val account = root.getValue("account").jsonObject
        assertThat(account.getValue("email").jsonPrimitive.content).isEqualTo(SignInAccount.localAccount.email)
        assertThat(account.getValue("displayName").jsonPrimitive.content).isEqualTo(SignInAccount.localAccount.displayName)

        val consent = root.getValue("consent").jsonObject
        assertThat(consent.array("purposes").map { it.jsonPrimitive.content }).containsExactly("AI_PROCESSING")
        assertThat(consent.getValue("noticeVersion").jsonPrimitive.content).isEqualTo(ConsentRecord.CURRENT_NOTICE_VERSION)

        val profile = root.getValue("profile").jsonObject
        assertThat(profile.getValue("fullName").jsonPrimitive.content).isEqualTo(canonicalCandidateProfile.fullName)
        assertThat(profile.array("skills")).hasSize(canonicalCandidateProfile.skills.size)
        val entries = profile.array("entries")
        assertThat(entries).hasSize(canonicalCandidateProfile.entries.size)
        assertThat(entries.first().jsonObject.keys).containsAtLeast("source", "isConfirmed", "bullets")

        val entitlement = root.getValue("entitlement").jsonObject
        assertThat(entitlement.getValue("freeCredits").jsonPrimitive.int).isEqualTo(1)
        assertThat(entitlement.getValue("purchasedCredits").jsonPrimitive.int).isEqualTo(4)
        val purchase = root.array("purchases").single().jsonObject
        assertThat(purchase.getValue("orderId").jsonPrimitive.content).isEqualTo("order-1")
        assertThat(purchase.getValue("packId").jsonPrimitive.content).isEqualTo("application_pack_5")
        assertThat(purchase.getValue("state").jsonPrimitive.content).isEqualTo("COMPLETED")
    }

    @Test
    fun noEntryHoldsATokenOrKey() {
        val everything = archive().values.joinToString("\n").lowercase()

        listOf("token", "obfuscatedaccountid", "apikey", "api_key").forEach { forbidden ->
            assertThat(everything).doesNotContain(forbidden)
        }
        assertThat(AccountData::class.java.declaredFields.map { it.name.lowercase() })
            .containsNoneOf("token", "purchasetoken", "obfuscatedaccountid", "apikey")
    }

    @Test
    fun applicationsTextShowsJdAnalysisResumeAndNotes() {
        val text = archive().getValue("applications.txt")

        assertThat(text).contains(application.job.rawText)
        assertThat(text).contains("Experience with Kotlin")
        assertThat(text).contains("MET")
        assertThat(text).contains("Built a Kotlin Android app")
        assertThat(text).contains("Call the recruiter on Tuesday")
        assertThat(text).contains("Dear Example Corp team")
        assertThat(text).contains("Revise coroutines")
    }

    @Test
    fun extraEntriesStayNextToTheLocalFiles() {
        val files = archive(mapOf("server.json" to "{}"))

        assertThat(files.keys).containsExactly(
            "account.txt",
            "profile.txt",
            "applications.txt",
            "purchases.txt",
            "my-data.json",
            "server.json",
        )
    }
}
