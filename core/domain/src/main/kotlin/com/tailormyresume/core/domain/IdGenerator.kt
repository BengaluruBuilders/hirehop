package com.tailormyresume.core.domain

fun interface IdGenerator {
    fun newId(): String
}
