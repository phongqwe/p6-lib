package com.emeraldblast.p6.message.api.connection.kernel_context.errors

import com.emeraldblast.p6.common.exception.error.ErrorHeader
import java.nio.file.Path

object KernelErrors {
    private const val prefix = "Kernel Error "
    object CantStartProcess {
        val header = ErrorHeader("${prefix}1", "Can't start kernel process")
        class Data (val startCommand:String)
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
        class Data(val additionalInfo:String)
    }

    object KernelServiceDown{
        val header = ErrorHeader("${prefix}6","a kernel service is down")
        class Data(val serviceName:String)
    }

    object GetKernelObjectError{
        val header = ErrorHeader("${prefix}7","can't get kernel object")
        class Data(val objectName:String)
    }

    object CantCreateKernelConfig{
        val header = ErrorHeader("${prefix}8","can't create kernel config")
        class Data(val exception:Exception)
    }
}
