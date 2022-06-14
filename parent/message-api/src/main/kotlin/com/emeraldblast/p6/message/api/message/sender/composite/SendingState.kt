package com.emeraldblast.p6.message.api.message.sender.composite

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.github.michaelbull.result.Result

/**
 * state of the sending action
 */
enum class SendingState {
    Start {
        override fun transit(
            hasResult: Boolean,
            kernelIsRunning: Boolean,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {
            return Working.transit(hasResult, kernelIsRunning, executionState)
        }
    },
    KernelDieMidway {
        override fun transit(
            hasResult: Boolean,
            kernelIsRunning: Boolean,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {
            return this
        }
    },
    Working {
        override fun transit(
            hasResult: Boolean,
            kernelIsRunning: Boolean,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {

            if (hasResult) {
                return HasResult
            }
            if (executionState == IOPub.Status.ExecutionState.idle) {
                return DoneButNoResult
            }
            if (kernelIsRunning.not()) {
                return KernelDieMidway
            }

            return this
        }
    },
    DoneButNoResult {
        override fun transit(
            hasResult: Boolean,
            kernelIsRunning: Boolean,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {
            return this
        }
    },
    HasResult {
        override fun transit(
            hasResult: Boolean,
            kernelIsRunning: Boolean,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {
            return this
        }
    }

    ;

    /**
     * transit state with direct value
     */
    abstract fun transit(
        hasResult: Boolean,
        kernelIsRunning: Boolean,
        executionState: IOPub.Status.ExecutionState,
    ): SendingState

    /**
     * interpret state from objects, then transit
     */
    fun transit(
        rt: Result<*, *>?,
        kernelContext: KernelContextReadOnly,
        executionState: IOPub.Status.ExecutionState,
    ): SendingState {

        return this.transit(
            hasResult = rt != null,
            kernelIsRunning = kernelContext.isKernelRunning(),
            executionState = executionState
        )
    }
}
