package com.emeraldblast.p6.message.api.connection.service.iopub.handler.execution_handler

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.service.iopub.handler.AbsDeferredJobHandler
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.emeraldblast.p6.message.api.message.sender.exception.SenderErrors
import javax.inject.Inject

class ExecuteErrorHandlerImp @Inject constructor() : AbsDeferredJobHandler<MessageHeader,ErrorReport>(), ExecuteErrorHandler {

    override val msgType: MsgType = IOPub.ExecuteError.msgType

    override fun handle(msg: JPRawMessage) {
        val receivedMsg: JPMessage<IOPub.ExecuteError.MetaData, IOPub.ExecuteError.Content> = msg.toModel()
        receivedMsg.parentHeader?.also {parentHeader->
            deferredJobMap[parentHeader]?.also {job->
                job.complete(
                    ErrorReport(
                        header = SenderErrors.IOPubExecuteError.header,
                        data = SenderErrors.IOPubExecuteError.Data(receivedMsg.content),
                    )
                )
                this.removeJob(parentHeader)
            }
        }
    }
}
