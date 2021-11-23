package com.github.xadkile.bicp.message.di

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


suspend fun main() {
    println("Before")
    suspendCoroutine<Unit> { con->
        println("Before too")
        con.resume(Unit)
    }

    println("After")
}
