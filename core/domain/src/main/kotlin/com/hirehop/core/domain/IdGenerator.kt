package com.hirehop.core.domain

fun interface IdGenerator {
    fun newId(): String
}
