package com.tailormyresume.core.domain.fact

data class FactDraftError(val field: FactField, val reason: FactDraftErrorReason)
