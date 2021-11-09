package com.github.xadkile.bicp.message.api.protocol.message

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.gson.annotations.SerializedName

enum class MsgStatus {
    ok,
    error,
    aborted
    ;
}
