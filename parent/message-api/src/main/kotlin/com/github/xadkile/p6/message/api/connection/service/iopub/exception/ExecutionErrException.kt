package com.github.xadkile.p6.message.api.connection.service.iopub.exception

import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.IOPub


class ExecutionErrException(val exceptionInfo: ExceptionInfo<IOPub.ExecuteError.Content>) :Exception(exceptionInfo.toString()){
    fun getData(): IOPub.ExecuteError.Content {
        return this.exceptionInfo.data
    }

}
