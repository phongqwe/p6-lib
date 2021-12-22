package com.github.xadkile.bicp.message.api.msg.protocol.exception

import com.github.xadkile.bicp.exception.ExceptionInfo

//class InvalidPayloadSizeException(size:Int):Exception("Invalid payload size (${size}), must be at least 6") {
//}

class InvalidPayloadSizeException(val exceptionInfo: ExceptionInfo<Int>):Exception(exceptionInfo.toString()) {
    constructor(size:Int, loc:Any):this(ExceptionInfo("Invalid payload size (${size}), must be at least 6",loc,size))


}


