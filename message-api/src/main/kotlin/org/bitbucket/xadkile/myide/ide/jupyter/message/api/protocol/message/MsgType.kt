package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

import arrow.core.Either
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

/**
 * msg_type is defined in jupyter document
 * https://jupyter-client.readthedocs.io/en/latest/messaging.html
 */
interface MsgType {
    enum class Control : MsgType {
        shutdown_request, shutdown_reply
    }

    enum class Shell : MsgType {
        execute_request, execute_reply
    }

    enum class IOPub : MsgType {
        display_data, execute_result
    }

    companion object {
        fun parse(type: String): Result<MsgType,IllegalArgumentException> {
            val all: List<MsgType> = (Control.values().asList()) + Shell.values().asList() + (IOPub.values().asList())
            try {
                val z = all.first { it.toString() == type }
                return Ok(z)
            } catch (e: NoSuchElementException) {
                return Err(IllegalArgumentException("${type} is not a MsgType"))
            }
        }
    }

}
