package com.hirehop.app.ai

import com.hirehop.core.domain.AiException
import com.hirehop.core.domain.AiFailure
import com.hirehop.core.domain.ResumeTailor
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.TailoredResume
import com.hirehop.core.network.ApiError
import com.hirehop.core.network.ApiException
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.apiResult
import com.hirehop.core.network.dto.TailoringDto
import com.hirehop.core.network.dto.TailoringResultDto
import com.hirehop.core.network.dto.TailoringStartRequest
import com.hirehop.core.network.dto.TailoringStatus
import com.hirehop.core.network.mapper.toDto
import com.hirehop.core.network.mapper.toFactsDto
import com.hirehop.core.network.mapper.toTailoredBullet
import kotlinx.coroutines.delay
import javax.inject.Inject

class RemoteResumeTailor @Inject constructor(
    private val api: HirehopApi,
    private val pending: PendingTailoringIds,
) : ResumeTailor {
    override suspend fun tailor(
        profile: CandidateProfile,
        job: JobDescription,
        gap: GapAnalysis,
        applicationId: String,
        section: EntryCategory?,
    ): TailoredResume {
        val request = TailoringStartRequest(
            requestId = pending.idFor(applicationId, section),
            applicationId = applicationId,
            job = job.toDto(),
            matches = gap.matches.map { it.toDto() },
            profile = profile.toFactsDto(),
            section = section,
        )
        try {
            val finished = awaitFinished(start(request))
            pending.clear(applicationId, section)
            val result = finished.result?.takeIf { finished.status == TailoringStatus.SUCCEEDED }
                ?: throw AiException(AiFailure.Unavailable)
            return result.toTailoredResume(profile)
        } catch (failure: AiException) {
            if (failure.failure !in RESUMABLE) pending.clear(applicationId, section)
            throw failure
        }
    }

    private suspend fun start(request: TailoringStartRequest): TailoringDto {
        var attempt = 1
        while (true) {
            val result = apiResult { api.startTailoring(request) }
            val error = (result.exceptionOrNull() as? ApiException)?.error
            if (error !is ApiError.RateLimited || attempt >= START_ATTEMPTS) return result.orAiFailure().tailoring
            attempt++
            delay((error.retryAfterSeconds ?: DEFAULT_RETRY_AFTER_SECONDS) * MILLIS_PER_SECOND)
        }
    }

    private suspend fun awaitFinished(started: TailoringDto): TailoringDto {
        var current = started
        var waited = 0L
        var step = FIRST_POLL_MILLIS
        while (current.status == TailoringStatus.RUNNING) {
            if (waited >= GIVE_UP_AFTER_MILLIS) throw AiException(AiFailure.Timeout)
            delay(step)
            waited += step
            current = remoteAi { api.tailoring(current.id) }.tailoring
            step = minOf(step + POLL_BACKOFF_MILLIS, MAX_POLL_MILLIS)
        }
        return current
    }

    private fun TailoringResultDto.toTailoredResume(profile: CandidateProfile): TailoredResume {
        val sourceText = profile.entries.filter { it.isConfirmed }.flatMap { it.bullets }.associate { it.id to it.text }
        return TailoredResume(
            bullets = bullets.map { bullet ->
                bullet.toTailoredBullet(bullet.sourceIds.firstNotNullOfOrNull(sourceText::get).orEmpty())
                    .copy(generationId = generationId)
            },
        )
    }

    private companion object {
        val RESUMABLE = setOf(AiFailure.Network, AiFailure.Timeout, AiFailure.Unavailable)
        const val START_ATTEMPTS = 3
        const val DEFAULT_RETRY_AFTER_SECONDS = 10
        const val MILLIS_PER_SECOND = 1_000L
        const val FIRST_POLL_MILLIS = 2_000L
        const val POLL_BACKOFF_MILLIS = 1_000L
        const val MAX_POLL_MILLIS = 5_000L

        // docs/BACKEND_CONTRACT.md line 4.4 rule 5: the server fails a RUNNING job after 5 minutes; 30 s of slack
        const val GIVE_UP_AFTER_MILLIS = 330_000L
    }
}
