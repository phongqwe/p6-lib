package com.github.xadkile.bicp.message.api.channel

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
        val tcp = com.github.xadkile.bicp.message.api.channel.ChannelInfo("", "tcp", "", -1)
    }
}

