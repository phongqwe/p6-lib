package com.github.xadkile.p6.exception.error

object Errors {
    object ERR1 : ErrorHeader(1,"this is Err1"){
        data class ERR1Data(val name:String, val location:String)
    }
}
