package com.hirehop.core.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRemovalNotice @Inject constructor() {
    private val mutableShowsBanner = MutableStateFlow(true)

    val showsBanner: StateFlow<Boolean> = mutableShowsBanner

    fun record(removedDateOfBirthOrPhoto: Boolean) {
        mutableShowsBanner.value = removedDateOfBirthOrPhoto
    }
}
