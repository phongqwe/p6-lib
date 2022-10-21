package com.qxdzbc.p6.message.api.message.protocol.data_interface_definition

import com.qxdzbc.p6.message.api.message.protocol.*

object Control {

    object Debug {
        // TODO later
    }

    object KernelInterrupt {
        object Request : MsgDefinitionEncapsulation {

            override val msgType = MsgType.Control_interrupt_request

            class Content : MsgContent

            class MetaData : MapMsgMetaData()
        }

        object Reply : MsgDefinitionEncapsulation {

            override val msgType = MsgType.Control_interrupt_reply

            class Content(
                status: MsgStatus,
                traceback: List<String>,
                ename: String,
                evalue: String,
            ) : MsgContent, CommonReplyContent(status, traceback, ename, evalue)

            class MetaData :MapMsgMetaData()
        }
    }

    object KernelShutdown {

        object Request : MsgDefinitionEncapsulation {
            override val msgType = MsgType.Control_shutdown_request

            class Content constructor(val restart: Boolean) : MsgContent
        }

        object Reply : MsgDefinitionEncapsulation {
            override val msgType = MsgType.Control_shutdown_reply

            class Content(
                status: MsgStatus,
                traceback: List<String>,
                ename: String,
                evalue: String,
                val restart: Boolean,
            ) : MsgContent, CommonReplyContent(status, traceback, ename, evalue)
        }
    }


}
