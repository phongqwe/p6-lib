package com.github.xadkile.p6.message.api.msg.sender.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class ZMQMsgTimeOutException(val exceptionInfo: ExceptionInfo<Nothing?>) : Exception(exceptionInfo.toString()) {

    constructor():this(ExceptionInfo("ZMQ msg timeout","",null))

    companion object {
        fun occurAt(o:Any):ZMQMsgTimeOutException{
            return ZMQMsgTimeOutException(ExceptionInfo("ZMQ msg timeout",o,null))
        }
    }
}
