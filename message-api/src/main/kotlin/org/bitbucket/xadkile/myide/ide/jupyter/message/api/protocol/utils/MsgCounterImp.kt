package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import java.util.concurrent.atomic.AtomicInteger

class MsgCounterImp : MsgCounter {
    private val counter = AtomicInteger(0)
    override fun next():Int{
        return counter.incrementAndGet()
    }
}
