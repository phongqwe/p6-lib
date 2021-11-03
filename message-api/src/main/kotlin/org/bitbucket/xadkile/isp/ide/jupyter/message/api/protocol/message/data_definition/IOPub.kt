package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_definition

import com.google.gson.annotations.SerializedName
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType
import javax.inject.Qualifier

object IOPub {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Address

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Channel

    object DisplayData {
        val msgType = MsgType.IOPub_display_data
        class Content(
            val data: Map<String, Any>,
            @SerializedName("metadata")
            val metaData: Map<String, Any>,
            val transient: Map<String, Any>
        ) : MsgContent
    }
}
