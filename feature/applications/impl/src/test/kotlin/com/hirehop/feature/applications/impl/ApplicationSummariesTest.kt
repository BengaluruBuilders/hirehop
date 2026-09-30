package com.hirehop.feature.applications.impl

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.TailoredResume
import org.junit.Test

class ApplicationSummariesTest {

    @Test
    fun toGapSummary_countsEachStatusAndListsOnlyMustHaveGaps() {
        val summary = testGapAnalysis().toGapSummary()

        assertThat(summary.met).isEqualTo(2)
        assertThat(summary.partial).isEqualTo(1)
        assertThat(summary.gap).isEqualTo(2)
        assertThat(summary.mustHaveGaps).containsExactly("Requirement d")
    }

    @Test
    fun toReviewProgress_ignoresBulletsWithoutChanges() {
        val progress = testTailoredResume().toReviewProgress()

        assertThat(progress).isEqualTo(ReviewProgress(reviewed = 2, total = 3))
    }

    @Test
    fun toReviewProgress_whenEveryChangeIsPending_reportsZeroReviewed() {
        val resume = TailoredResume(
            bullets = listOf(
                testBullet("1", BulletDecision.PENDING, listOf(EditType.SHORTEN)),
                testBullet("2", BulletDecision.PENDING, listOf(EditType.MERGE)),
            ),
        )

        assertThat(resume.toReviewProgress()).isEqualTo(ReviewProgress(reviewed = 0, total = 2))
    }

    @Test
    fun toReviewProgress_whenNoBulletChanged_reportsZeroTotal() {
        val resume = TailoredResume(
            bullets = listOf(testBullet("1", BulletDecision.PENDING, editTypes = emptyList())),
        )

        assertThat(resume.toReviewProgress()).isEqualTo(ReviewProgress(reviewed = 0, total = 0))
    }
}
