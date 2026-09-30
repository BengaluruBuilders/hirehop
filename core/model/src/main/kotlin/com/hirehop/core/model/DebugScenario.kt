package com.hirehop.core.model

enum class DebugScenario {
    DEFAULT,
    LOADING,
    EMPTY,
    OFFLINE,
    ERROR,
    PARTIAL,
    SUCCESS,
    SCANNED,
    IMPORTED,
    FULLY_CONFIRMED,
    PARTLY_CONFIRMED,
    USER_STATED,
    DELETING,
    EXPORTING,
    PURCHASED,
    PENDING,
    CANCELLED,
    RESTORED,
    ;

    companion object {
        val defaultValue: DebugScenario = DEFAULT
    }
}
