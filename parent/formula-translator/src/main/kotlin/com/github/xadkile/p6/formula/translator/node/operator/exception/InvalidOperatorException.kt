package com.github.xadkile.p6.formula.translator.node.operator.exception

import com.github.xadkile.p6.exception.lib.ExceptionInfo
import com.github.xadkile.p6.formula.translator.errors.FailToParseFormulaException

class InvalidOperatorException (val info: ExceptionInfo<Char>): FailToParseFormulaException()
