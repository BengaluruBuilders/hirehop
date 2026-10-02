package com.hirehop.feature.tailor.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun jobLine(title: String, company: String): String? {
    val role = title.trim()
    val employer = company.trim()
    if (role.isEmpty() && employer.isEmpty()) return null
    return stringResource(
        R.string.feature_tailor_impl_job_subtitle,
        role.ifEmpty { stringResource(R.string.feature_tailor_impl_role_not_set) },
        employer.ifEmpty { stringResource(R.string.feature_tailor_impl_company_not_set) },
    )
}
