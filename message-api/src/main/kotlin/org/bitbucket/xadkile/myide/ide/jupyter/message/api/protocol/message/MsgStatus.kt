package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.gson.annotations.SerializedName

enum class MsgStatus {
    ok,
    error,
    aborted
    ;
//    companion object {
//        fun parse(input:String):Result<MsgStatus,Exception>{
//            val status = try {
//                Ok(valueOf(input))
//            } catch (e: Exception) {
//                Err(e)
//            }
//            return status
//        }
//    }
}
