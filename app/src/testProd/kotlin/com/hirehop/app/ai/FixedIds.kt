package com.hirehop.app.ai

import com.hirehop.core.domain.IdGenerator

object FixedIds : IdGenerator {
    private var next = 0

    override fun newId(): String = "request-${++next}"
}
