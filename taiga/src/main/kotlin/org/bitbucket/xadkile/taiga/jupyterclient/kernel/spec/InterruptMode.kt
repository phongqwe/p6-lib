package org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec

import com.google.gson.annotations.SerializedName

enum class InterruptMode {
    @SerializedName("signal")
    signal,

    @SerializedName("message")
    message,

    NOT_YET
}

