package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message

interface PayloadMaker {
    /**
     * make payload that will be sent to zmq
     */
    fun makePayload():List<ByteArray>
}
