package test.utils

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import com.github.michaelbull.result.*
import org.junit.jupiter.api.Test

class Bench {
    @Test
    fun  z(){
        val k:Either<Exception,Int> = Either.Right(1)
        val r: Result<Int,IllegalStateException> = Err(IllegalStateException("zz"))
        // get right or throw
        k.bind()
        val c1 = r.component1()
        val c2 = r.component2()
        val o = r.map { 1 }
        val o2 = r.andThen {it->Ok("zx")}
        // what I want is: get the inside value if this is a right, otherwise, return the Left<nested Exception>
        // of course the value and exception cannot be hold inside one-type variable
        // so the only way to get both at the same time is using Result object
        // so what does it look it?
        // 1. Have a result
        // 2. use one of this:
        //      - "when" to access value and run side-effect
        //      - use provided method to create resulting Result (map, andThen,...), then return the result
        when(r){
            is Ok -> println(r.value)
            is Err-> println(r.error.message)
        }
    }
}
