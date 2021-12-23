package com.github.xadkile.p6.formula.translator.node.operator.exception

import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.formula.translator.exception.FailToParseFormulaException

class InvalidOperatorException (val info: ExceptionInfo<Char>): FailToParseFormulaException()
