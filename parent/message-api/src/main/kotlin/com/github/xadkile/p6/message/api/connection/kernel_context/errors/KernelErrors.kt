package com.github.xadkile.p6.message.api.connection.kernel_context.errors

import com.github.xadkile.p6.common.exception.error.ErrorType
import java.nio.file.Path

object KernelErrors {
    private const val prefix = "Kernel Error "
    object CantStartProcess : ErrorType("${prefix}1", "Can't start kernel process"){
        class Data (val startCommand:String)
    }

    object CantStopKernelProcess : ErrorType("${prefix}2","Can't stop kernel process"){
        class Data(val processID:Long?)
    }

    object CantWriteConnectionFile : ErrorType("${prefix}3", "Can't write connection file to disk"){
        class Data(val connectionFilePath:Path?)
    }

    object KernelContextIllegalState : ErrorType("${prefix}4","Kernel is in an illegal state to perform certain action"){
        class Data(val currentState:String, val actionToPerform:String)
    }

    object KernelDown: ErrorType("${prefix}5","Kernel is down"){
        class Data(val additionalInfo:String)
    }

    object KernelServiceDown: ErrorType("${prefix}6","a kernel service is down"){
        class Data(val serviceName:String)
    }

    object GetKernelObjectError: ErrorType("${prefix}7","can't get kernel object"){
        class Data(val objectName:String)
    }

    object CantCreateKernelConfig: ErrorType("${prefix}8","can't create kernel config"){
        class Data(val exception:Exception)
    }
}
