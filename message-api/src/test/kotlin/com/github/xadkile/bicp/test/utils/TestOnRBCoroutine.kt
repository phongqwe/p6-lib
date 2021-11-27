package com.github.xadkile.bicp.test.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

/**
 * For running test on runBlocking{...}
 */
abstract class TestOnRBCoroutine {
    lateinit var mainThreadSurrogate: ExecutorCoroutineDispatcher
    @BeforeAll
    fun beforeAll(){
        mainThreadSurrogate= newSingleThreadContext("Test Thread")
        Dispatchers.setMain(mainThreadSurrogate)

    }
    @AfterAll
    fun afterAll(){
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}
