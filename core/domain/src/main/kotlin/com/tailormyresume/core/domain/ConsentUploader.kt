package com.tailormyresume.core.domain

import com.tailormyresume.core.model.ConsentRecord

fun interface ConsentUploader {
    suspend fun upload(record: ConsentRecord): Result<Unit>
}
