package com.hirehop.core.data.model

import com.hirehop.core.database.json.GuardrailViolationDto
import com.hirehop.core.database.json.TailoredBulletDto
import com.hirehop.core.database.json.TailoredResumeDto
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume

fun GuardrailViolation.asDto(): GuardrailViolationDto = when (this) {
    GuardrailViolation.MissingSource -> GuardrailViolationDto.MissingSource
    is GuardrailViolation.UnsupportedNumber -> GuardrailViolationDto.UnsupportedNumber(value)
    is GuardrailViolation.UnsupportedTerm -> GuardrailViolationDto.UnsupportedTerm(term)
    is GuardrailViolation.VerbEscalation -> GuardrailViolationDto.VerbEscalation(from, to)
    is GuardrailViolation.UnsupportedScaleClaim -> GuardrailViolationDto.UnsupportedScaleClaim(phrase)
}

fun GuardrailViolationDto.asExternalModel(): GuardrailViolation = when (this) {
    GuardrailViolationDto.MissingSource -> GuardrailViolation.MissingSource
    is GuardrailViolationDto.UnsupportedNumber -> GuardrailViolation.UnsupportedNumber(value)
    is GuardrailViolationDto.UnsupportedTerm -> GuardrailViolation.UnsupportedTerm(term)
    is GuardrailViolationDto.VerbEscalation -> GuardrailViolation.VerbEscalation(from, to)
    is GuardrailViolationDto.UnsupportedScaleClaim -> GuardrailViolation.UnsupportedScaleClaim(phrase)
}

fun TailoredBullet.asDto() = TailoredBulletDto(
    id = id,
    entryId = entryId,
    originalText = originalText,
    proposedText = proposedText,
    sourceIds = sourceIds,
    editTypes = editTypes,
    keywordsUsed = keywordsUsed,
    violations = violations.map(GuardrailViolation::asDto),
    decision = decision,
)

fun TailoredBulletDto.asExternalModel() = TailoredBullet(
    id = id,
    entryId = entryId,
    originalText = originalText,
    proposedText = proposedText,
    sourceIds = sourceIds,
    editTypes = editTypes,
    keywordsUsed = keywordsUsed,
    violations = violations.map(GuardrailViolationDto::asExternalModel),
    decision = decision,
)

fun TailoredResume.asDto() = TailoredResumeDto(bullets = bullets.map(TailoredBullet::asDto))

fun TailoredResumeDto.asExternalModel() = TailoredResume(
    bullets = bullets.map(TailoredBulletDto::asExternalModel),
)
