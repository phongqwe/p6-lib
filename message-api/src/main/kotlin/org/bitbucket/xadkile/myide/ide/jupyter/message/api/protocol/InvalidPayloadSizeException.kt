package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

class InvalidPayloadSizeException(size:Int):Exception("Invalid payload size (${size}), must be at least 6") {
}
