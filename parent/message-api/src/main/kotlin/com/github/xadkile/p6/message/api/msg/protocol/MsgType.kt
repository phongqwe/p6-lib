package com.github.xadkile.p6.message.api.msg.protocol

import com.google.gson.annotations.SerializedName

/**
 * msg_type is defined in jupyter document
 * https://jupyter-client.readthedocs.io/en/latest/messaging.html
 */
enum class MsgType {
    @SerializedName("interrupt_request")
    Control_interrupt_request {
        override fun text(): String {
           return "interrupt_request"
        }
    },

    @SerializedName("interrupt_reply")
    Control_interrupt_reply {
        override fun text(): String {
            return "interrupt_reply"
        }
    },

    @SerializedName("shutdown_request")
    Control_shutdown_request {
        override fun text(): String {
            return "shutdown_request"
        }
    },
    @SerializedName("shutdown_reply")
    Control_shutdown_reply {
        override fun text(): String {
            return "shutdown_reply"
        }
    },
    @SerializedName("execute_request")
    Shell_execute_request {
        override fun text(): String {
            return "execute_request"
        }
    },
    @SerializedName("execute_reply")
    Shell_execute_reply {
        override fun text(): String {
            return "execute_reply"
        }
    },
    @SerializedName("kernel_info_request")
    Shell_kernel_info_request {
        override fun text(): String {
            return "kernel_info_request"
        }
    },
    @SerializedName("kernel_info_reply")
    Shell_kernel_info_reply {
        override fun text(): String {
            return "kernel_info_reply"
        }
    },
    @SerializedName("display_data")
    IOPub_display_data {
        override fun text(): String {
            return "display_data"
        }
    },
    @SerializedName("execute_result")
    IOPub_execute_result {
        override fun text(): String {
            return "execute_result"
        }
    },
    @SerializedName("status")
    IOPub_status {
        override fun text(): String {
            return "status"
        }
    },

    @SerializedName("error")
    IOPub_error{
        override fun text(): String {
            return "error"
        }
    },


    DEFAULT {
        override fun text(): String {
            return "DEFAULT"
        }
    }

    ;

    abstract fun text():String
}
