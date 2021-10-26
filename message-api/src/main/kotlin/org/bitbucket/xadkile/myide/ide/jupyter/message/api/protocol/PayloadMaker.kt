package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

interface PayloadMaker {
    fun makePayload():List<ByteArray>
}
