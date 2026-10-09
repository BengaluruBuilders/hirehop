package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.DebugScenario
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForcedPaymentScenario @Inject constructor() {
    @Volatile
    var scenario: DebugScenario = DebugScenario.DEFAULT
}
