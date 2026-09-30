package com.hirehop.core.domain.fact

import com.hirehop.core.model.ProfileEntry

sealed interface AddFactsOutcome {
    data class Added(val entries: List<ProfileEntry>) : AddFactsOutcome
    data class Rejected(val errors: List<FactDraftError>) : AddFactsOutcome
    data object NothingToAdd : AddFactsOutcome
}
