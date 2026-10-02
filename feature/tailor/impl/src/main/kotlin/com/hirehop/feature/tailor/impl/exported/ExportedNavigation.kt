package com.hirehop.feature.tailor.impl.exported

import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.tailor.api.navigation.BulletReviewNavKey
import com.hirehop.feature.tailor.api.navigation.CoverLetterNavKey
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.api.navigation.PackPurchaseNavKey
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.hirehop.feature.tailor.api.navigation.TailorNavKey

internal fun Navigator.finishExport(applicationId: String) {
    var canPop = state.currentKey.isPartOfExport(applicationId)
    while (canPop && goBack()) {
        canPop = state.currentKey.isPartOfExport(applicationId)
    }
    navigate(ApplicationDetailNavKey(applicationId = applicationId))
}

internal fun Navigator.openApplicationWorkspace(applicationId: String) {
    val workspace = ApplicationDetailNavKey(applicationId = applicationId)
    if (workspace in state.currentSubStack) {
        var canPop = true
        while (canPop && state.currentKey != workspace) {
            canPop = goBack()
        }
    } else {
        navigate(workspace)
    }
}

private fun Any.isPartOfExport(applicationId: String): Boolean = when (this) {
    is ExportedNavKey -> this.applicationId == applicationId
    is ExportPreviewNavKey -> this.applicationId == applicationId
    is PackPurchaseNavKey -> this.applicationId == applicationId
    is TailorNavKey -> this.applicationId == applicationId
    is BulletReviewNavKey -> this.applicationId == applicationId
    is CoverLetterNavKey -> this.applicationId == applicationId
    is PrepQuestionsNavKey -> this.applicationId == applicationId
    else -> false
}
