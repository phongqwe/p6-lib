package org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender

interface Message {
    fun getPayload():List<ByteArray>
}
