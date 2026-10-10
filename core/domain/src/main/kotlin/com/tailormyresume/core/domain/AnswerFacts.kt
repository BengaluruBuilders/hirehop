package com.tailormyresume.core.domain

import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.QuickAnswer

internal object AnswerFacts {
    const val YES_REGULARLY = "YES_REGULARLY"
    const val A_FEW_TIMES = "A_FEW_TIMES"

    fun isAnswerId(id: String): Boolean = false

    fun keywords(answer: QuickAnswer?, job: JobDescription): List<String> = emptyList()

    fun factOf(answer: QuickAnswer?, job: JobDescription): EvidenceBullet? = null
}
