sealed class Token<T>(open val value: T) {
    object Plus: Token<Char>('+')
    object Minus: Token<Char>('-')
    object Multiply: Token<Char>('*')
    object Divide: Token<Char>('/')
    object Dot: Token<Char>('.')
    object Pow: Token<Char>('^')
    object Assignment: Token<Char>('=')

    data class Identifier(override val value: String): Token<String>(value) {

        override fun toString() = "Var($value)"

    }
    data class DoubleLiteral(override val value: String) : Token<String>(value) {
        override fun toString() = value.toString()
    }

    override fun toString() = value.toString()
}