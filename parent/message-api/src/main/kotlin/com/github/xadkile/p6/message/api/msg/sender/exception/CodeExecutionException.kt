package com.github.xadkile.p6.message.api.msg.sender.exception

import com.github.xadkile.p6.exception.ExceptionInfo

sealed class CodeExecutionException(val displayMessage:String) :Exception() {
    class Error(displayMessage: String="Code execution error") : CodeExecutionException(displayMessage)
    class Aborted(displayMessage: String="code execution aborted"):CodeExecutionException(displayMessage)
}
