package com.github.xadkile.p6.common.exception.error

open class ErrorHeader(val errorCode: String, val errorDescription: String){
    override fun toString(): String {
        return "${errorCode}: $errorDescription"
    }
    fun isType(errorHeader: ErrorHeader):Boolean{
        return this.errorCode == errorHeader.errorCode
    }
}
