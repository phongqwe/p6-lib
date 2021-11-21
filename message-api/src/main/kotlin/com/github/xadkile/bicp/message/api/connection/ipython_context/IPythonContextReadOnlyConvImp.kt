package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result

class IPythonContextReadOnlyConvImp(private val context: IPythonContextReadOnly) : IPythonContextReadOnlyConv {
    override fun original(): IPythonContextReadOnly {
        return this.context
    }

    override fun conv(): IPythonContextReadOnlyConv {
        return this
    }

}
