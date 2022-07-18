package com.emeraldblast.p6.message.api.connection.kernel_context.errors

import com.emeraldblast.p6.common.exception.error.ErrorHeader
import com.emeraldblast.p6.common.exception.error.ErrorReport
import java.nio.file.Path

object KernelErrors {
    private const val prefix = "Kernel Error "
    object CantStartKernelProcess {
        val header = ErrorHeader("${prefix}1", "Can't start kernel process")
        class Data (val startCommand:String)
        fun reportForCommand(command:String):ErrorReport{
            return header.setDescription("Can't start kernel process with this command: ${command}").toErrorReport()
        }
    }

    object CantStopKernelProcess {
        val header= ErrorHeader("${prefix}2","Can't stop kernel process")
        class Data(val processID:Long?)
    }

    object CantWriteConnectionFile {
        val header=ErrorHeader("${prefix}3", "Can't write connection file to disk")
        class Data(val connectionFilePath:Path?)
    }

    object KernelContextIllegalState {
        val header = ErrorHeader("${prefix}4","Kernel is in an illegal state to perform certain action")
        class Data(val currentState:String, val actionToPerform:String)
    }

    object KernelDown{
        val header=ErrorHeader("${prefix}5","Kernel is down")
        fun report(detail:String):ErrorReport{
            val report = ErrorReport(
                header = header.setDescription(detail),
            )
            return report
        }
    }

    object KernelServiceDown{
        val header = ErrorHeader("${prefix}6","a kernel service is down")
    }

    object KernelConfigIsNull{
        val header = ErrorHeader("${prefix}7","kernel config is null")
        fun report(detail: String):ErrorReport{
            return header.setDescription(detail).toErrorReport()
        }
    }

    object CantCreateKernelConfig{
        val header = ErrorHeader("${prefix}8","can't create kernel config")
        class Data(val exception:Exception)
    }


}
