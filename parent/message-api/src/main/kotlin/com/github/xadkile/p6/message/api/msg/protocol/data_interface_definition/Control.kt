package com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition

import com.github.xadkile.p6.message.api.msg.protocol.MsgContent
import com.github.xadkile.p6.message.api.msg.protocol.MsgMetaData
import com.github.xadkile.p6.message.api.msg.protocol.MsgStatus
import com.github.xadkile.p6.message.api.msg.protocol.MsgType

object Control {

    object Debug {
        // TODO later
    }

    object KernelInterrupt {
        object Request : MsgDefinitionEncapsulation {

            override val msgType = MsgType.Control_interrupt_request

            class Content : MsgContent

            class MetaData : MsgMetaData
        }

        object Reply : MsgDefinitionEncapsulation {

            override val msgType = MsgType.Control_interrupt_reply

            class Content(
                status: MsgStatus,
                traceback: List<String>,
                ename: String,
                evalue: String,
            ) : MsgContent, CommonReplyContent(status, traceback, ename, evalue)

            class MetaData : MsgMetaData {}
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
