package com.github.xadkile.p6.message.api.system

/**
 * React to system events
 */
interface SystemEventReactor {
    fun onSystemEvent(event:SystemEvent)
}

enum class SystemEvent {
//    fun getCode():Int
}
