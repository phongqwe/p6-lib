package org.bitbucket.xadkile.taiga.jupyterclient.client.message

import org.bitbucket.xadkile.taiga.TimeUtils
import java.util.*


interface MessageHeader {
    fun getMsgId():String
    fun getMsgType():String
    fun getUsername():String
    fun getSessionId():String
    fun getDate():Date
    fun getVersion():String
    fun toFacade():Facade
    /**
     * placeholder class only, must not add any function to this interface
     */
    interface Facade{
    }
}

