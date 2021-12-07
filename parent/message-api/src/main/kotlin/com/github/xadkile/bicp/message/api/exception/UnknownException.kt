package com.github.xadkile.bicp.message.api.exception


class UnknownException(exceptionInfo:ExceptionInfo) : ExceptionWithInfo(exceptionInfo){
    companion object {
        fun occurAt(o:Any): UnknownException {
            return UnknownException("occur at ${o.javaClass.simpleName}")
        }
    }

    constructor(message:String):this(ExceptionInfo(message,"",null))
}

