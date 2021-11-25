import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CPrimitiveVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValues
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.objcPtr
import platform.posix.exit
import platform.posix.free
import kotlin.math.PI
import kotlin.math.pow
import kotlin.native.concurrent.freeze

typealias Action = (Token<*>) -> String?

class Parser(val tokens: MutableList<Token<*>>, val debug: Boolean = false, val localVars: HashMap<String, Double>) {

    val actions = mutableListOf<Action>()

    init {
        actions.add {
            when (it) {
                is Token.Identifier -> {
                    when (it.value) {
                        "exit" -> exit(0).let { "Exiting application" }
                        "clear" -> localVars.forEach { (key, _) ->
                            if(key != "pi") {
                                localVars.remove(key)
                            }
                        }
                            .let {
                                "Cleared local variables"
                            }
                        "vars" -> localVars.toString()
                        else -> null
                    }
                }
                else -> null
            }
        }
    }

    fun buildNumber(): String {
        printStep("Tokenized expression: ")
        tokens.forEach {
            actions.forEach { action ->
                val result = action(it)
                if (result != null) return result
            }
        }
        if (tokens.firstOrNull() != null && tokens.first() is Token.Identifier && tokens.size > 1 && tokens[1] == Token.Assignment) return assign()
        while (tokens.any { it is Token.Identifier }) destructureVariables()
        while (Token.Dot in tokens) removeDots()
        while (Token.Pow in tokens) removePows()
        while (Token.Multiply in tokens || Token.Divide in tokens) leaveRawNumbers()
        val iterator = tokens.iterator()
        var num = 0.0
        var currentAction: Token<*> = Token.Plus
        while (iterator.hasNext()) {
            val token = iterator.next()
            if (token is Token.DoubleLiteral) {
                when (currentAction) {
                    Token.Plus -> {
                        printStep("Add ${token.value} to $num: ")
                        num += token.value.toDouble()
                    }
                    Token.Minus -> {
                        printStep("Subtract $num by ${token.value}: ")
                        num -= token.value.toDouble()
                    }
                    else -> Unit
                }
            } else {
                currentAction = token
            }
        }
        return num.toString()
    }

    private fun assign(): String {
        val identifier = tokens.first() as Token.Identifier
        val parser = Parser(tokens.subList(2, tokens.size), debug, localVars)
        val number = parser.buildNumber().toDouble()
        localVars[identifier.value] = number
        return "Assigned $number to ${identifier.value}"
    }

    private fun removePows() {
        for ((i, token) in tokens.toList().withIndex()) {
            if (token is Token.Pow) {
                val first = tokens[i - 1]
                val second = tokens[i + 1]
                if (first is Token.DoubleLiteral && second is Token.DoubleLiteral) {
                    tokens[i - 1] = Token.DoubleLiteral(first.value.toDouble().pow(second.value.toDouble()).toString())
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    printStep("Destructure $first ^ $second: ")
                    break
                }
            }
        }
    }

    private fun destructureVariables() {
        for ((i, token) in tokens.toList().withIndex()) {
            if (token is Token.Identifier) {
                val value = localVars[token.value]
                if (value != null) {
                    tokens[i] = Token.DoubleLiteral(value.toString())
                    printStep("Destructure variable ${token.value}: ")
                } else {
                    throw IllegalStateException("Unknown variable: ${token.value}")
                }
            }
        }
    }

    private fun removeDots() {
        for ((i, token) in tokens.toList().withIndex()) {
            if (token is Token.Dot) {
                val first = tokens[i - 1]
                val second = tokens[i + 1]
                if (first is Token.DoubleLiteral && second is Token.DoubleLiteral) {
                    tokens[i - 1] = Token.DoubleLiteral("${first.value}.${second.value}")
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    printStep("Remove Dot for $first and $second: ")
                    break
                }
            }
        }
    }

    private fun leaveRawNumbers() {
        for ((i, token) in tokens.toList().withIndex()) {
            if (token is Token.Multiply) {
                val first = tokens[i - 1]
                val second = tokens[i + 1]
                if (first is Token.DoubleLiteral && second is Token.DoubleLiteral) {
                    tokens[i - 1] = Token.DoubleLiteral((first.value.toDouble() * second.value.toDouble()).toString())
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    printStep("Multiply $first and $second: ")
                    break
                }
            } else if (token is Token.Divide) {
                val first = tokens[i - 1]
                val second = tokens[i + 1]
                if (first is Token.DoubleLiteral && second is Token.DoubleLiteral) {
                    tokens[i - 1] = Token.DoubleLiteral((first.value.toDouble() / second.value.toDouble()).toString())
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    printStep("Divide $first by $second: ")
                    break
                }
            }
        }
    }

    private fun printStep(description: String) = if (debug) println("$description $tokens") else Unit

}