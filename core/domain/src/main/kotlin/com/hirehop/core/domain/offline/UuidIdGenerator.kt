package com.hirehop.core.domain.offline

import com.hirehop.core.domain.IdGenerator
import java.util.UUID
import javax.inject.Inject

internal class UuidIdGenerator @Inject constructor() : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
