package com.github.xadkile.p6.common

import com.github.xadkile.p6.common.CanCheckEmpty

interface HaveSize : CanCheckEmpty {
    val size:Int
    override fun isEmpty(): Boolean {
        return size!=0
    }
}
