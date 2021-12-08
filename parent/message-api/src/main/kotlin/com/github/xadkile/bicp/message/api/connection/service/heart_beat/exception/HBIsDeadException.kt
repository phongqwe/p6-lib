package com.github.xadkile.bicp.message.api.connection.service.heart_beat.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

/**
 * HB signal is dead
 */
class HBIsDeadException (val exceptionInfo: ExceptionInfo<Any> ):Exception(exceptionInfo.toString()) {
}

