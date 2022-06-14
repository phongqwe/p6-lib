package com.emeraldblast.p6.message.api.connection.service.iopub.handler.execution_handler

import com.emeraldblast.p6.message.api.connection.service.iopub.handler.DeferredJobHandler
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub

/**
 * A handler for catching execution status
 */
interface ExecutionStatusHandler : DeferredJobHandler<MessageHeader,IOPub.Status.ExecutionState>
