package com.tailormyresume.feature.settings.impl.settings

import android.content.Context
import android.content.Intent

internal fun supportMailIntent(address: String, subject: String): Intent? = null

internal fun Context.openSupportMail(address: String, subject: String): Boolean = false
