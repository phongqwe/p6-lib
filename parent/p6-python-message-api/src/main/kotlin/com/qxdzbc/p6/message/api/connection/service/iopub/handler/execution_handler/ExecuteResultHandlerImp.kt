package com.qxdzbc.p6.message.api.connection.service.iopub.handler.execution_handler

import com.qxdzbc.p6.message.api.connection.service.iopub.handler.AbsDeferredJobHandler
import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import com.qxdzbc.p6.message.api.message.protocol.MessageHeader
import com.qxdzbc.p6.message.api.message.protocol.MsgType
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.qxdzbc.p6.message.api.message.sender.composite.ExecuteResult
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

/**
 * handle execute_result push
 */
class ExecuteResultHandlerImp @Inject constructor() : ExecuteResultHandler,
    AbsDeferredJobHandler<MessageHeader, ExecuteResult>() {

    override val msgType: MsgType = IOPub.ExecuteResult.msgType

    override fun handle(msg: JPRawMessage) {
        val receivedMsg: ExecuteResult = msg.toModel()
        val parentHeader = receivedMsg.parentHeader
        parentHeader?.also {
            val job: CompletableDeferred<ExecuteResult>? = deferredJobMap[it]
            job?.also {
                job.complete(receivedMsg)
            }
            this.removeJob(parentHeader)
        }
    }
}
