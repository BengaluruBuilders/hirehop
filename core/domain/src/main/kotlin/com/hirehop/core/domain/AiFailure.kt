package com.hirehop.core.domain

enum class AiFailure {
    Network,
    Timeout,
    ConsentRequired,
    NoCredit,
    AllowanceExhausted,
    InvalidInput,
    AccountDeleted,
    Unavailable,
}

class AiException(val failure: AiFailure) : Exception(failure.name)

fun Throwable.isAiFailure(failure: AiFailure): Boolean = (this as? AiException)?.failure == failure
