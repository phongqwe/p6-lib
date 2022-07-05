package com.emeraldblast.p6.formula

import kotlin.reflect.KFunction

data class FunctionMapImp(
    private val m:Map<String,KFunction<Any>>
) : FunctionMap, Map<String, KFunction<Any>> by m {

    override fun getFunc(name: String): KFunction<Any>? {
        return m[name]
    }

    override fun addFunc(name: String, func: KFunction<Any>): FunctionMap {
        return this.copy(m = m + (name to func))
    }

    override fun removeFunc(name: String): FunctionMap {
        return this.copy(m = m - (name))
    }
}
