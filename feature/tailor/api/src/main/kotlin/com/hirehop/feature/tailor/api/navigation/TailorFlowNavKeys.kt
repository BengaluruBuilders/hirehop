package com.hirehop.feature.tailor.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class BulletReviewNavKey(
    val applicationId: String,
    val bulletId: String? = null,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class CoverLetterNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class PrepQuestionsNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

fun Navigator.navigateToBulletReview(applicationId: String, bulletId: String? = null) {
    navigate(BulletReviewNavKey(applicationId = applicationId, bulletId = bulletId))
}

fun Navigator.navigateToCoverLetter(applicationId: String) {
    navigate(CoverLetterNavKey(applicationId))
}

fun Navigator.navigateToPrepQuestions(applicationId: String) {
    navigate(PrepQuestionsNavKey(applicationId))
}
