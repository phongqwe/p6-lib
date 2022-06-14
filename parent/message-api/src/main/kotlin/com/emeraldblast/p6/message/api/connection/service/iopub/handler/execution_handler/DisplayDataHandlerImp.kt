package com.emeraldblast.p6.message.api.connection.service.iopub.handler.execution_handler

import com.emeraldblast.p6.message.api.connection.service.iopub.handler.AbsDeferredJobHandler
import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub
import javax.inject.Inject

class DisplayDataHandlerImp @Inject constructor() : DisplayDataHandler, AbsDeferredJobHandler<MessageHeader, IOPub.DisplayData.Content>() {
    override val msgType: MsgType = IOPub.DisplayData.msgType
    override fun handle(msg: JPRawMessage) {
        val modelMsg = msg.toModel<
                IOPub.DisplayData.MetaData,
                IOPub.DisplayData.Content>()
        modelMsg.parentHeader?.also {parentHeader->
            this.deferredJobMap[parentHeader]?.also {job->
                job.complete(modelMsg.content)
                this.removeJob(parentHeader)
            }
        }
    }
}
