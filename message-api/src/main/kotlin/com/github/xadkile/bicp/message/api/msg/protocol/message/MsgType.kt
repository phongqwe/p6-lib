package com.github.xadkile.bicp.message.api.msg.protocol.message

import com.google.gson.annotations.SerializedName

/**
 * msg_type is defined in jupyter document
 * https://jupyter-client.readthedocs.io/en/latest/messaging.html
 */
enum class MsgType {
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


    NOT_RECOGNIZE {
        override fun text(): String {
            return "NOT_RECOGNIZE"
        }
    }

    ;

    abstract fun text():String
}
