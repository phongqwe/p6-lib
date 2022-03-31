package com.emeraldblast.p6.message.api.message.protocol

import com.google.gson.annotations.SerializedName

enum class MsgStatus {
    @SerializedName("ok")
    OK,
    @SerializedName("error")
    ERROR,
    @SerializedName("aborted")
    ABORTED,
    ;
}
