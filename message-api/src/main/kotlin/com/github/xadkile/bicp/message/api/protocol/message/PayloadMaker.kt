package com.github.xadkile.bicp.message.api.protocol.message

interface PayloadMaker {
    /**
     * make payload that will be sent to zmq
     */
    fun makePayload():List<ByteArray>
}
