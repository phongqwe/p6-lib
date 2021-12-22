package com.github.xadkile.bicp.formula.translator.node.operator.exception

import com.github.xadkile.bicp.exception.ExceptionInfo
import com.github.xadkile.bicp.formula.translator.exception.FormulaParseException

class InvalidOperatorException (val info:ExceptionInfo<Char>): FormulaParseException()
