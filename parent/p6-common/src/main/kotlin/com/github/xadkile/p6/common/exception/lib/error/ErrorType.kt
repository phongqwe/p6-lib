package com.github.xadkile.p6.common.exception.lib.error

open class ErrorType(val errorCode: String, val errorDescription: String){
    override fun toString(): String {
        return """
            ErrorCode: ${errorCode},
            Description: $errorDescription
        """.trimIndent()
    }
    fun isType(errorType: ErrorType):Boolean{
        return this.errorCode == errorType.errorCode
    }
}
