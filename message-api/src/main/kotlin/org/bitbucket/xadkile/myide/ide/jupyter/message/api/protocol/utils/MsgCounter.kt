package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BinaryOperator
import java.util.function.IntBinaryOperator

interface MsgCounter {
    fun next():Int
}


