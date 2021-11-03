package org.bitbucket.xadkile.isp.ide.jupyter.message.api.session

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.channel.ChannelInfo
import java.nio.file.Path

/**
 * There's a problem with this DI approach:
 *      if python is killed in the middle of operation, all of these objects will be useless because they all rely on a dead python instance.
 *      The following need to be recreated:
 *      - Session
 *      - All ChannelInfo object
 *      - Connection file
 *      - All senders
 *  I need an ability to re-connect to running python instance
 *      Maybe, a class to handle this out of DI structure.
 *      This class must reflect the current state of connection.
 *      The state of connection can be changed at any moment (disconnect, new connection,etc), so this class must be mutable and update its content using a single source of truth (a fixed path to a connection file)
 *
 *      An instance can be provided by the DI, but connection must be established using manual code
 *      derived objects cannot be provided by DI containers (or can it? Maybe it can. But should I ?)
 *
 *      This class also need to be able to broadcast events to those that listen => need observable/observer pattern => might need coroutin for multi-threading shit.
 *
 *
 */
class Session(
    var connectionFilePath:Path,
) {

//    fun reConnect():Session{}
//    fun connect():Session{}
//    fun getIOPubChannel(): ChannelInfo {}
//    fun getShellChannel():ChannelInfo{}
//    fun getControlChannel():ChannelInfo{}
}
