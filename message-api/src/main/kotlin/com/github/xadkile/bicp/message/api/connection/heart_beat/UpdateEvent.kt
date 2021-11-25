package com.github.xadkile.bicp.message.api.connection.heart_beat

fun interface UpdateEvent {
    fun consum(): UpdateSignal
}
