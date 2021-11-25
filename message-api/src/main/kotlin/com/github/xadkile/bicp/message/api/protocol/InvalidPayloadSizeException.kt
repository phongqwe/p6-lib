package com.github.xadkile.bicp.message.api.protocol

class InvalidPayloadSizeException(size:Int):Exception("Invalid payload size (${size}), must be at least 6") {
}
