package com.qxdzbc.p6.message.api.connection.service.iopub.handler.execution_handler

import com.qxdzbc.p6.message.api.connection.service.iopub.handler.DeferredJobHandler
import com.qxdzbc.p6.message.api.message.protocol.MessageHeader
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub

interface DisplayDataHandler : DeferredJobHandler<MessageHeader, IOPub.DisplayData.Content> {

}

