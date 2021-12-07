package com.github.xadkile.bicp.message.api.connection.service.iopub.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.IOPub


class ExecutionErrException(val exceptionInfo: ExceptionInfo<IOPub.ExecuteError.Content>) :Exception(exceptionInfo.toString()){
    fun getData(): IOPub.ExecuteError.Content {
        return this.exceptionInfo.data
    }

}
