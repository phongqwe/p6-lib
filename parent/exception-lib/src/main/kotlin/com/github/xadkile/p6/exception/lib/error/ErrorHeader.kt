package com.github.xadkile.p6.exception.lib.error

open class ErrorHeader(val errorCode: String,  val errorDescription: String){
    override fun toString(): String {
        return """
            ErrorCode: ${errorCode},
            Description: $errorDescription
        """.trimIndent()
    }
}
