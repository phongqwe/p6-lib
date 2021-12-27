package com.github.xadkile.p6.message.api.connection.kernel_context.exception

import com.github.xadkile.p6.exception.error.ErrorHeader
import java.nio.file.Path

object KernelErrors {
    object CantStartProcess : ErrorHeader("CantStartProcess".hashCode(), "Can't start kernel process"){
        class Data (val startCommand:String)
    }

    object CantStopKernelProcess : ErrorHeader("CantStopKernelProcess".hashCode(),"Can't stop kernel process"){
        class Data(val processID:Long?)
    }

    object CantWriteConnectionFile : ErrorHeader("CantWriteConnectionFile".hashCode(), "Can't write connection file to disk"){
        class Data(val connectionFilePath:Path?)
    }

    object KernelContextIllegalState : ErrorHeader("KernelContextIllegalState".hashCode(),"Kernel is in an illegal state to perform certain action"){
        class Data(val currentState:String, val actionToPerform:String)
    }

    object KernelDown: ErrorHeader("KernelDown".hashCode(),"Kernel is down"){
        class Data(val additionalInfo:String)
    }

    object KernelServiceDown:ErrorHeader("KernelServiceDown".hashCode(),"a kernel service is down"){
        class Data(val serviceName:String)
    }

    object GetKernelObjectError:ErrorHeader("GetKernelObjectError".hashCode(),"can't get kernel object"){
        class Data(val objectName:String)
    }
}
