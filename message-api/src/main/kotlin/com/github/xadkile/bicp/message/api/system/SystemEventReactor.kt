package com.github.xadkile.bicp.message.api.system

/**
 * React to system events
 */
interface SystemEventReactor {
    fun onSystemEvent(event:SystemEvent)
}

interface SystemEvent {
    fun getCode():Int
}
