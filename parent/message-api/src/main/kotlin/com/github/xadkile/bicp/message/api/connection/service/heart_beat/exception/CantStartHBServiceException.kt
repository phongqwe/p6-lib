package com.github.xadkile.bicp.message.api.connection.service.heart_beat.exception

import com.github.xadkile.bicp.exception.ExceptionInfo

class CantStartHBServiceException (val exceptionInfo: ExceptionInfo<Any> ):Exception(exceptionInfo.toString()) {
}

