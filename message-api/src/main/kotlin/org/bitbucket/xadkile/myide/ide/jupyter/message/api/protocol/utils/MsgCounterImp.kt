package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MsgCounterImp  @Inject constructor() : MsgCounter {
    private val counter = AtomicInteger(0)
    override fun next():Int{
        return counter.incrementAndGet()
    }
}
