import kotlin.math.PI

class Lexer(val debug: Boolean = false) {

    val localVars: HashMap<String, Double> = hashMapOf()

    init {
        localVars["pi"] = PI
    }

    fun parse(line: String) : String {
        val tokens = mutableListOf<Token<*>>()
        val iterator = line.iterator()
        while(iterator.hasNext()) {
            nextToken(iterator, tokens)
        }
        return Parser(tokens, debug, localVars).buildNumber()
    }

    private fun nextToken(iterator: Iterator<Char>, tokens: MutableList<Token<*>>): Unit = when(val position = iterator.next()) {
        '+' -> tokens += Token.Plus
        '-' -> tokens += Token.Minus
        '*' -> tokens += Token.Multiply
        '/' -> tokens += Token.Divide
        '.' -> tokens += Token.Dot
        '^' -> tokens += Token.Pow
        '=' -> tokens += Token.Assignment
        in 'a'..'z', in 'A'..'B', '_' -> {
            if(tokens.isNotEmpty() && tokens.last() is Token.Identifier) {
                val token = tokens.last() as Token.Identifier
                tokens.removeLast()
                tokens += token.copy(value = token.value + position)
            } else {
                tokens += Token.Identifier(position.toString())
            }
        }
        in '0'..'9' -> {
            if(tokens.isNotEmpty() && tokens.last() is Token.DoubleLiteral) {
                val token = tokens.last() as Token.DoubleLiteral
                tokens.removeLast()
                val newValue = token.value + position
                tokens += token.copy(value = newValue)
            } else {
                tokens += Token.DoubleLiteral(position.toString())
            }
        }
        ' ' -> Unit
        else -> println("Illegal character: $position")
    }


}