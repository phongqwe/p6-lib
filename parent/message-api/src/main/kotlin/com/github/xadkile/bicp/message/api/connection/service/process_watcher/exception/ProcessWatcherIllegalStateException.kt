package com.github.xadkile.bicp.message.api.connection.service.process_watcher.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

class ProcessWatcherIllegalStateException(val exceptionInfo:ExceptionInfo<Nothing?>) : Exception(exceptionInfo.toString()){
    constructor(message:String):this(ExceptionInfo(message,"",null))

}
