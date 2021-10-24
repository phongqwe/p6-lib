package org.bitbucket.xadkile.myide.ide.jupyterclient.kernel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KernelJsonTest{
    @Test
    fun fromJson(){
        val json = "{\n" +
                " \"argv\": [\"a\",\"b\",\"c\"" +
                " ],\n" +
                " \"display_name\": \"Python 3\",\n" +
                " \"language\": \"python\"\n"+
                "}"
        assertDoesNotThrow {
            val o = KernelJson.fromJson(json)
            assertEquals(listOf("a","b","c"),o.argv)
            assertEquals("Python 3",o.displayName)
            assertEquals("python",o.language)
        }
    }


    @Test
    fun k(){
        val l1 = listOf(1,2,3,4)
        val l2 = listOf<Int>()
        val out = l1.flatMap { n1->
            l2.map{
                n1*it
            }
        }
        println(out)
    }
}
