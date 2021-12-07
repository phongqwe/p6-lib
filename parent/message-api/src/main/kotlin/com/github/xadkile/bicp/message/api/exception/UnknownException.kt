package com.github.xadkile.bicp.message.api.exception


class UnknownException(val exceptionInfo:ExceptionInfo<*>) : Exception(exceptionInfo.toString()){
    companion object {
        fun occurAt(o:Any): UnknownException {
            return UnknownException(ExceptionInfo("unknow exception",o,null))
        }
    }

    constructor(msg:String):this(ExceptionInfo(
        msg=msg,
        loc="",
        data =null
    ))
}

