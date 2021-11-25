import platform.posix.pow
import kotlin.math.PI
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.nanoseconds

fun main(args: Array<String>) {
    val debug = "--debug" in args
    val lexer = Lexer(debug)
    println("Math Parser (Jan) " + if(debug) "[DEBUG]" else "")
    println("Supported actions: Plus (+), Minus (-), Multiply (*), Divide (/), Pow (^), Variable Assigning")
    print(">>> ")
    while(true) {
        val input = readln()
        val time = measureNanoTime {
            try {
                val result = lexer.parse(input)
                println(result)
            } catch(e: Exception) {
                println(e)
            }
        }
        if(debug) println("Time: ${time.nanoseconds}")
        print(">>> ")
    }
}
