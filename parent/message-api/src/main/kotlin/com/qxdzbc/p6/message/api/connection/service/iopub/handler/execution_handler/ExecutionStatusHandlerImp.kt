package com.qxdzbc.p6.message.api.connection.service.iopub.handler.execution_handler

import com.qxdzbc.p6.message.api.connection.service.iopub.handler.AbsDeferredJobHandler
import com.qxdzbc.p6.message.api.message.protocol.JPMessage
import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import com.qxdzbc.p6.message.api.message.protocol.MessageHeader
import com.qxdzbc.p6.message.api.message.protocol.MsgType
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

sealed class ExecutionStatusHandlerImp constructor(private val targetStatus: IOPub.Status.ExecutionState) : ExecutionStatusHandler, AbsDeferredJobHandler<MessageHeader,IOPub.Status.ExecutionState>() {

    class Idle @Inject constructor() : ExecutionStatusHandlerImp(IOPub.Status.ExecutionState.idle),
        IdleExecutionStatusHandler
    class Busy @Inject constructor(): ExecutionStatusHandlerImp(IOPub.Status.ExecutionState.busy),
        BusyExecutionStatusHandler

    override val msgType: MsgType = IOPub.Status.msgType

    override fun handle(msg: JPRawMessage) {
        val receivedMsg: JPMessage<IOPub.Status.MetaData, IOPub.Status.Content> = msg.toModel()
        val executionState: IOPub.Status.ExecutionState = receivedMsg.content.executionState
        if (executionState == targetStatus) {
            val parentHeader: MessageHeader? = receivedMsg.parentHeader
            parentHeader?.also {
                val job: CompletableDeferred<IOPub.Status.ExecutionState>? = deferredJobMap[it]
                job?.also {
                    job.complete(executionState)
                }
                this.removeJob(parentHeader)
            }
        }
    }
}
