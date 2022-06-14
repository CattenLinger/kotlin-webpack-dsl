package webpack.utils

class OraSpinner(text : String) {

    private val spinner = ora(text)

    fun start() = spinner.start()

    fun stop() = spinner.stop()

    companion object {
        private val ora = js("require('ora')")
    }
}