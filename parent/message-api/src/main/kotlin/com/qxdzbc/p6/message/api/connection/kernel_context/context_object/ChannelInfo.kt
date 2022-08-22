package com.qxdzbc.p6.message.api.connection.kernel_context.context_object

/**
 * Hold channel profile info that can be used for establishing connection
 */
data class ChannelInfo(
    val name:String,
    val protocol:String,
    val ipAddress:String,
    val port:Int
){
    fun makeAddress():String{
        return "$protocol://$ipAddress:$port"
    }

    companion object {
        val tcp = ChannelInfo("", "tcp", "", -1)
    }
}

