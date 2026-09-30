package com.hirehop.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val hhDispatcher: HhDispatchers)

enum class HhDispatchers {
    Default,
    IO,
}
