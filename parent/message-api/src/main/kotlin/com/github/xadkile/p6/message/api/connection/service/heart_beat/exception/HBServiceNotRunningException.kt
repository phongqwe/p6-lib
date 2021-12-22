package com.github.xadkile.p6.message.api.connection.service.heart_beat.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class HBServiceNotRunningException (val exceptionInfo: ExceptionInfo<Any>):Exception(exceptionInfo.toString()) {
}

