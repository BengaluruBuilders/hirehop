package com.tailormyresume.core.model

val EvidenceBullet.isTooLong: Boolean get() = text.length > ProfileLimits.MAX_BULLET_LENGTH

val ProfileEntry.hasTooLongBullet: Boolean get() = bullets.any { it.isTooLong }

fun fitBulletsToLimit(texts: List<String>): List<String> = texts
