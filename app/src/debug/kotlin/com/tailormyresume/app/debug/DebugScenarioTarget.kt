package com.tailormyresume.app.debug

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.app.R
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.analysis.api.navigation.AnalysisNavKey
import com.tailormyresume.feature.applications.api.navigation.ApplicationDetailNavKey
import com.tailormyresume.feature.applications.api.navigation.ApplicationsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ImportResumeNavKey
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
import com.tailormyresume.feature.onboarding.api.navigation.WelcomeNavKey
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.api.navigation.ProfileNavKey
import com.tailormyresume.feature.settings.api.navigation.AccountDeletedNavKey
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import com.tailormyresume.feature.settings.api.navigation.SettingsNavKey
import com.tailormyresume.feature.settings.api.navigation.YourDataNavKey
import com.tailormyresume.feature.tailor.api.navigation.BulletReviewNavKey
import com.tailormyresume.feature.tailor.api.navigation.CoverLetterNavKey
import com.tailormyresume.feature.tailor.api.navigation.CreditsNavKey
import com.tailormyresume.feature.tailor.api.navigation.ExportPreviewNavKey
import com.tailormyresume.feature.tailor.api.navigation.ExportedNavKey
import com.tailormyresume.feature.tailor.api.navigation.PackPurchaseNavKey
import com.tailormyresume.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.tailormyresume.feature.tailor.api.navigation.TailorNavKey

const val SAMPLE_APPLICATION_ID = "sample-northwind-associate-analyst"

enum class DebugScenarioTarget(
    @param:StringRes val titleRes: Int,
    val opensFirstRunRoot: Boolean = false,
) {
    Welcome(R.string.debug_target_welcome, opensFirstRunRoot = true),
    PasteJobDescription(R.string.debug_target_paste_job_description, opensFirstRunRoot = true),
    SignIn(R.string.debug_target_sign_in, opensFirstRunRoot = true),
    Consent(R.string.debug_target_consent, opensFirstRunRoot = true),
    ImportResume(R.string.debug_target_import_resume, opensFirstRunRoot = true),
    ConfirmFacts(R.string.debug_target_confirm_facts, opensFirstRunRoot = true),
    Applications(R.string.debug_target_applications),
    ApplicationDetail(R.string.debug_target_application_detail),
    Profile(R.string.debug_target_profile),
    FactEditor(R.string.debug_target_fact_editor),
    GuidedForm(R.string.debug_target_guided_form),
    FactEvidence(R.string.debug_target_fact_evidence),
    Analysis(R.string.debug_target_analysis),
    Tailor(R.string.debug_target_tailor),
    BulletReview(R.string.debug_target_bullet_review),
    CoverLetter(R.string.debug_target_cover_letter),
    PrepQuestions(R.string.debug_target_prep_questions),
    ExportPreview(R.string.debug_target_export_preview),
    PackPurchase(R.string.debug_target_pack_purchase),
    Exported(R.string.debug_target_exported),
    Credits(R.string.debug_target_credits),
    Settings(R.string.debug_target_settings),
    YourData(R.string.debug_target_your_data),
    DeleteAccount(R.string.debug_target_delete_account),
    ;

    val needsSampleJob: Boolean get() = this == Analysis

    val forcesPayment: Boolean get() = this == PackPurchase || this == Credits

    fun navKey(scenario: DebugScenario): NavKey = when (this) {
        Welcome -> WelcomeNavKey(scenario = scenario)
        PasteJobDescription -> PasteJobDescriptionNavKey(scenario = scenario)
        SignIn -> SignInNavKey(scenario = scenario)
        Consent -> ConsentNavKey(scenario = scenario)
        ImportResume -> ImportResumeNavKey(scenario = scenario)
        ConfirmFacts -> ConfirmFactsNavKey(scenario = scenario)
        Applications -> ApplicationsNavKey(scenario = scenario)
        ApplicationDetail -> ApplicationDetailNavKey(SAMPLE_APPLICATION_ID, scenario)
        Profile -> ProfileNavKey(scenario = scenario)
        FactEditor -> FactEditorNavKey(entryId = null, scenario = scenario)
        GuidedForm -> GuidedProfileFormNavKey(scenario = scenario)
        FactEvidence -> FactEvidenceNavKey(scenario = scenario)
        Analysis -> AnalysisNavKey(scenario = scenario)
        Tailor -> TailorNavKey(SAMPLE_APPLICATION_ID, scenario)
        BulletReview -> BulletReviewNavKey(SAMPLE_APPLICATION_ID, scenario = scenario)
        CoverLetter -> CoverLetterNavKey(SAMPLE_APPLICATION_ID, scenario)
        PrepQuestions -> PrepQuestionsNavKey(SAMPLE_APPLICATION_ID, scenario)
        ExportPreview -> ExportPreviewNavKey(applicationId = SAMPLE_APPLICATION_ID, scenario = scenario)
        PackPurchase -> PackPurchaseNavKey(applicationId = SAMPLE_APPLICATION_ID, scenario = scenario)
        Exported -> ExportedNavKey(applicationId = SAMPLE_APPLICATION_ID, scenario = scenario)
        Credits -> CreditsNavKey(scenario = scenario)
        Settings -> SettingsNavKey(scenario = scenario)
        YourData -> YourDataNavKey(scenario = scenario)
        DeleteAccount -> if (scenario == DebugScenario.SUCCESS) {
            AccountDeletedNavKey
        } else {
            DeleteAccountNavKey(scenario = scenario)
        }
    }
}
