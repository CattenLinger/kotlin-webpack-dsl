package webpack.utils

object ColoredConsole {
    private val chalk = js("require('chalk')")

    fun green(text : String) {
        console.log(chalk.green(text))
    }

    fun red(text: String) {
        console.log(chalk.red(text))
    }

    fun cyan(s: String) {
        console.log(chalk.cyan(s))
    }

    fun yellow(s : String) {
        console.log(chalk.yellow(s))
    }
}