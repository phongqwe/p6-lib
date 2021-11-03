package org.bitbucket.xadkile.isp.ide.jupyterclient.kernel

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

/**
 * kernel.json file
 */
data class KernelJson(
    val argv:List<String>,
    @SerializedName("display_name")
    val displayName:String,
    val language:String
) {
    // TODO for Gson parsing, but can be removed if I use no-arg plugin, maybe later
    constructor():this(emptyList(),"","")

    companion object CO{
        fun fromJson(json:String):KernelJson{
            val gson = GsonBuilder().create()
            return gson.fromJson(json,KernelJson::class.java)
        }

//        fun findIn
    }
}
