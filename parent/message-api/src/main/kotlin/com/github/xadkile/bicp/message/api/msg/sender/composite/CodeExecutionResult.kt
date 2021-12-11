package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.IOPub


class CodeExecutionResult(val hasResult:Boolean, val result:ExecuteResult) {
}
