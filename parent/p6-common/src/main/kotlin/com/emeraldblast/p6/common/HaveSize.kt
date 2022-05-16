package com.emeraldblast.p6.common


interface HaveSize : CanCheckEmpty {
    val size:Int
    override fun isEmpty(): Boolean {
        return size==0
    }
}
