package com.emeraldblast.p6.formula.execution_unit

import com.emeraldblast.p6.formula.FunctionMap
import kotlin.math.pow
import kotlin.reflect.KClass

/**
 * An execution unit is a provider obj that when run will return something
 */
interface ExUnit {

    /**
     * when this run, it returns something
     */
    fun run(): Any?

    companion object {
        val Nothing = object : ExUnit {
            override fun run(): Any? {
                return null
            }
        }

        val TRUE = Bool(true)
        val FALSE = Bool(false)
    }
    class Add(val u1: ExUnit, val u2: ExUnit) : ExUnit {
        override fun run(): Any? {
            val r1 = u1.run()
            val r2 = u2.run()
            if (r1 is Number && r2 is Number) {
                return r1.toDouble() + (r2.toDouble())
            } else {
                return null
            }
        }
    }

    class Sub(val u1: ExUnit, val u2: ExUnit) : ExUnit {
        override fun run(): Any? {
            val r1 = u1.run()
            val r2 = u2.run()
            if (r1 is Number && r2 is Number) {
                return r1.toDouble() - (r2.toDouble())
            } else {
                return null
            }
        }
    }



    class Mul(val u1: ExUnit, val u2: ExUnit) : ExUnit {
        override fun run(): Any? {
            val r1 = u1.run()
            val r2 = u2.run()
            if (r1 is Number && r2 is Number) {
                return r1.toDouble() * (r2.toDouble())
            } else {
                return null
            }
        }
    }

    class Div(val u1: ExUnit, val u2: ExUnit) : ExUnit {
        override fun run(): Any? {
            val r1 = u1.run()
            val r2 = u2.run()
            if (r1 is Number && r2 is Number) {
                return r1.toDouble() / (r2.toDouble())
            } else {
                return null
            }
        }
    }

    class PowerBy(val u1: ExUnit, val u2: ExUnit) : ExUnit {
        override fun run(): Any? {
            val r1 = u1.run()
            val r2 = u2.run()
            if (r1 is Number && r2 is Number) {
                return r1.toDouble().pow(r2.toDouble())
            } else {
                return null
            }
        }
    }

    class UnarySubtract(val u: ExUnit) : ExUnit {
        override fun run(): Any? {
            val rs = u.run()
            return when (rs) {
                is Int -> -rs
                is Double -> -rs
                is Float -> -rs
                else -> null
            }
        }
    }

    sealed class NumberUnit(val v: Number) : ExUnit {
        override fun run(): Number {
            return v
        }
    }

    class DoubleNum(v: Double) : NumberUnit(v) {
        override fun run(): Double {
            return v as Double
        }
    }

    class IntNum(v: Int) : NumberUnit(v) {
        override fun run(): Int {
            return this.v as Int
        }
    }

    class Text(val v: String) : ExUnit {
        override fun run(): String {
            return v
        }
    }

    class Bool(val v: Boolean) : ExUnit {
        override fun run(): Boolean {
            return v
        }
    }

    class Func(
        val funcName: String,
        val args: List<ExUnit>,
        val functionMap: FunctionMap,
    ) : ExUnit {
        override fun run(): Any? {
            val argValue = (args.map { it.run() }.toTypedArray())
            val func = functionMap.getFunc(funcName)
            return func?.call(*argValue)
        }
    }
}
