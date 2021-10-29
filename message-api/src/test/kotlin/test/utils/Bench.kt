package test.utils

import com.github.michaelbull.result.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.junit.jupiter.api.Test
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgStatus
import java.util.*


class Bench {
    class Z(
        val id:String,
        val date: Date,
        val status:MsgStatus,
        val info:Map<String,Any>
    )
    @Test
    fun  z(){
        val json = """
            {
                "id":"idz",
                "date":"2021-10-29T14:28:16.000123Z",
                "status":"ok",
                "info":{
                    "data":123, "name":"abc",
                    "nested":{
                        "x":10,
                        "y":-2
                    }
                }
            }
        """.trimIndent()
        val m:Z = Gson().fromJson(json, Z::class.java)
        for(p in m.info){
            println("${p.key}: ${p.value}")
        }
        println(m.status)
    }
}
