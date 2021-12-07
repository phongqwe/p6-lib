package com.github.xadkile.bicp.message.api.msg.protocol.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.exception.ExceptionWithInfo

//class InvalidPayloadSizeException(size:Int):Exception("Invalid payload size (${size}), must be at least 6") {
//}

class InvalidPayloadSizeException(exceptionInfo: ExceptionInfo):ExceptionWithInfo(exceptionInfo) {
    constructor(size:Int):this(ExceptionInfo("Invalid payload size (${size}), must be at least 6","","actual size is $size"))
}


