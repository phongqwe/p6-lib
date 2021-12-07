package com.github.xadkile.bicp.message.api.connection.service.process_watcher.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.exception.ExceptionWithInfo

class ProcessWatcherIllegalStateException(exceptionInfo:ExceptionInfo) : ExceptionWithInfo(exceptionInfo) {

    constructor(message:String):this(ExceptionInfo(message,"",null))

}
