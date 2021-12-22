package com.github.xadkile.bicp.message.api.msg.sender.exception

import com.github.xadkile.bicp.exception.ExceptionInfo

class ZMQMsgTimeOutException(val exceptionInfo: ExceptionInfo<Nothing?>) : Exception(exceptionInfo.toString()) {

    constructor():this(ExceptionInfo("ZMQ msg timeout","",null))

    companion object {
        fun occurAt(o:Any):ZMQMsgTimeOutException{
            return ZMQMsgTimeOutException(ExceptionInfo("ZMQ msg timeout",o,null))
        }
    }
}
