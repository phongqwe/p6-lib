package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.inRequest

import com.google.gson.Gson
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent

/**
 * Reading directly from zmq socket
 */
class RawRequest(
    val identities:String,
    val delimiter:String,
    val hmacSig:String,
    val header:String,
    val parentHeader:String,
    val metaData:String,
    val content:String,
    val buffer:ByteArray
)
