package com.github.xadkile.bicp.message.api.protocol.message

import com.google.gson.annotations.SerializedName

/**
 * msg_type is defined in jupyter document
 * https://jupyter-client.readthedocs.io/en/latest/messaging.html
 */
enum class MsgType {
    @SerializedName("shutdown_request")
    Control_shutdown_request,
    @SerializedName("shutdown_reply")
    Control_shutdown_reply,

    @SerializedName("execute_request")
    Shell_execute_request,
    @SerializedName("execute_reply")
    Shell_execute_reply,

    @SerializedName("display_data")
    IOPub_display_data,
    @SerializedName("execute_result")
    IOPub_execute_result,

    ;
}
