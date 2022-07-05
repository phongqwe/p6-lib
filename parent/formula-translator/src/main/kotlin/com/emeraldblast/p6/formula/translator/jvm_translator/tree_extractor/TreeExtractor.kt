package com.emeraldblast.p6.formula.translator.jvm_translator.tree_extractor

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.github.michaelbull.result.Result
import org.antlr.v4.runtime.tree.ParseTree
interface TreeExtractor{
    fun extractTree(formula: String): Result<ParseTree,ErrorReport>
}

