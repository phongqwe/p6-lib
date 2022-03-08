package com.github.xadkile.p6.message.api.message.protocol

import com.google.gson.annotations.SerializedName

/**
 * msg_type is defined in jupyter document
 * https://jupyter-client.readthedocs.io/en/latest/messaging.html
 */
enum class MsgType {
    // CONTROL
    @SerializedName("interrupt_request")
    Control_interrupt_request {
        override val text: String = "interrupt_request"
    },

    @SerializedName("interrupt_reply")
    Control_interrupt_reply {
        override val text: String = "interrupt_reply"
    },

    @SerializedName("shutdown_request")
    Control_shutdown_request {
        override val text: String = "shutdown_request"
    },
    @SerializedName("shutdown_reply")
    Control_shutdown_reply {
        override val text: String = "shutdown_reply"
    },

    // SHELL
    @SerializedName("comm_msg")
    Shell_comm_msg {
        override val text: String = "comm_msg"
    },
    @SerializedName("comm_close")
    Shell_comm_close {
        override val text: String = "comm_close"
    },
    @SerializedName("comm_open")
    Shell_comm_open {
        override val text: String = "comm_open"
    },
    @SerializedName("execute_request")
    Shell_execute_request {
        override val text: String = "execute_request"
    },
    @SerializedName("execute_reply")
    Shell_execute_reply {
        override val text: String = "execute_reply"
    },
    @SerializedName("kernel_info_request")
    Shell_kernel_info_request {
        override val text: String = "kernel_info_request"
    },
    @SerializedName("kernel_info_reply")
    Shell_kernel_info_reply {
        override val text: String = "kernel_info_reply"
    },

    //IOPUB
    @SerializedName("display_data")
    IOPub_display_data {
        override val text: String = "display_data"
    },
    @SerializedName("execute_result")
    IOPub_execute_result {
        override val text: String = "execute_result"
    },
    @SerializedName("status")
    IOPub_status {
        override val text: String ="status"
    },

    @SerializedName("error")
    IOPub_error{
        override val text: String = "error"
    },


    DEFAULT {
        override val text: String = "DEFAULT"
    }

    ;

    abstract val text:String
}
