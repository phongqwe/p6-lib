package com.github.xadkile.p6.message.api.connection.service.process_watcher.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class ProcessWatcherIllegalStateException(val exceptionInfo: ExceptionInfo<Nothing?>) : Exception(exceptionInfo.toString()){
    constructor(message:String):this(ExceptionInfo(message,"",null))

}
