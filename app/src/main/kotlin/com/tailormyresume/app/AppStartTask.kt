package com.tailormyresume.app

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

fun interface AppStartTask {
    fun start()
}

@Module
@InstallIn(SingletonComponent::class)
interface AppStartTasksModule {
    @Multibinds
    fun appStartTasks(): Set<AppStartTask>
}
