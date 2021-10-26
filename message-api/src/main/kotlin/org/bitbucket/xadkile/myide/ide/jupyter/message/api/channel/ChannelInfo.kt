package org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel

/**
 * Hold channel profile info that can be used for establishing connection
 */
data class ChannelInfo(
    val name:String,
    val protocol:String,
    val ipAddress:String,
    val port:Int
):ChannelAddressMaker{
    override fun makeAddress():String{
        return "$protocol://$ipAddress:$port"
    }

    companion object {
        val tcp = ChannelInfo("","tcp","",-1)
        fun localControl(port:Int): ChannelInfo {
            return tcp.copy(name="Control","127.0.0.1")
        }
    }
}

