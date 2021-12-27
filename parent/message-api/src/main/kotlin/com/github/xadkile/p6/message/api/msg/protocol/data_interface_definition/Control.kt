package com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition

import com.github.xadkile.p6.message.api.msg.protocol.MsgContent
import com.github.xadkile.p6.message.api.msg.protocol.MsgMetaData
import com.github.xadkile.p6.message.api.msg.protocol.MsgStatus
import com.github.xadkile.p6.message.api.msg.protocol.MsgType

object Control {

    object Debug {
        // TODO later
    }

    object KernelInterrupt{
        object Request : MsgDefinitionEncapsulation {

            val _msgType = MsgType.Control_interrupt_request

            class Content : MsgContent

            class MetaData : MsgMetaData

            override fun getMsgType(): MsgType {
                return _msgType
            }
        }

        object Reply : MsgDefinitionEncapsulation {

            val _msgType = MsgType.Control_interrupt_reply

            class Content(
                status: MsgStatus,
                traceback: List<String>,
                ename:String,
                evalue:String
            ) : MsgContent, CommonReplyContent(status, traceback, ename, evalue)

            class MetaData : MsgMetaData {}

            override fun getMsgType(): MsgType {
                return _msgType
            }
        }
    }

    object KernelShutdown {

        object Request: MsgDefinitionEncapsulation {

            val _msgType = MsgType.Control_shutdown_request

            class Content constructor(val restart: Boolean) : MsgContent

            override fun getMsgType(): MsgType {
                return _msgType
            }
        }

        object Reply: MsgDefinitionEncapsulation {

            val _msgType = MsgType.Control_shutdown_reply

            class Content(
                status: MsgStatus,
                traceback: List<String>,
                ename: String,
                evalue: String,
                val restart: Boolean,
            ) : MsgContent, CommonReplyContent(status, traceback, ename, evalue)

            override fun getMsgType(): MsgType {
                return _msgType
            }
        }
    }


}
