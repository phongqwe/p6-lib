package com.github.xadkile.p6.app.coderunner

import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface CodeRunner {
    /**
     * run a piece of cell code, return a representative String
     */
    suspend fun run(code:String, dispatcher:CoroutineDispatcher = Dispatchers.IO):Result<String,Exception>
}
