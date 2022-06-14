package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub

class DisplayDataHandler : AbsDeferredJobHandler<Any>() {
    override val msgType: MsgType = IOPub.DisplayData.msgType
    override fun handle(msg: JPRawMessage) {
        println(msg)
    }
}
