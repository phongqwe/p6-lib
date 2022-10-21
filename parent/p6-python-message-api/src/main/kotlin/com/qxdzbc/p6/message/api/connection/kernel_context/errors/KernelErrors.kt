package com.qxdzbc.p6.message.api.connection.kernel_context.errors

import com.qxdzbc.common.error.ErrorHeader
import com.qxdzbc.common.error.ErrorReport
import java.nio.file.Path

object KernelErrors {
    private const val prefix = "Kernel Error "
    object CantStartKernelProcess {
        val header = ErrorHeader("${prefix}1", "Can't start kernel process")
        class Data (val startCommand:String)
        fun reportForCommand(command:String): ErrorReport {
            return header.setDescription("Can't start kernel process with this command: ${command}").toErrorReport()
        }
    }

    object CantStopKernelProcess {
        val header= ErrorHeader("${prefix}2","Can't stop kernel process")
        class Data(val processID:Long?)
    }

    object CantWriteConnectionFile {
        val header= ErrorHeader("${prefix}3", "Can't write connection file to disk")
        class Data(val connectionFilePath:Path?)
        fun report(detail:String): ErrorReport {
            return header.setDescription(detail).toErrorReport()
        }
    }

    object KernelContextIllegalState {
        val header = ErrorHeader("${prefix}4","Kernel context is in an illegal state to perform certain action")
        class Data(val currentState:String, val actionToPerform:String)
        fun report(detail:String): ErrorReport {
            return header.setDescription(detail).toErrorReport()
        }
    }

    object KernelDown{
        val header= ErrorHeader("${prefix}5","Kernel is down")
        fun report(detail:String): ErrorReport {
            val report = ErrorReport(
                header = header.setDescription(detail),
            )
            return report
        }
    }

    object KernelProcessIsNotAvailable{
        val header = ErrorHeader("${prefix}6","kernel process is not available because either the kernel is down, or the kernel is not under the management of the app")
        fun report(): ErrorReport {
            return header.toErrorReport()
        }
    }

    object KernelConfigIsNull{
        val header = ErrorHeader("${prefix}7","kernel config is null")
        fun report(detail: String): ErrorReport {
            return header.setDescription(detail).toErrorReport()
        }
    }

    object CantCreateKernelConfig{
        val header = ErrorHeader("${prefix}8","can't create kernel config")
        class Data(val exception:Exception)
    }

    object CantStartKernelContext{
        val header = ErrorHeader("${prefix}9","can't start kernel context")
        fun report(detail: String): ErrorReport {
            return KernelConfigIsNull.header.setDescription(detail).toErrorReport()
        }
    }
}
