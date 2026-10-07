package com.hirehop.core.domain

import com.hirehop.core.model.ConsentRecord

fun interface ConsentUploader {
    suspend fun upload(record: ConsentRecord): Result<Unit>
}
