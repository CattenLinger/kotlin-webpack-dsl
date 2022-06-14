package webpack.devserver

import webpack.utils.ColoredConsole
import process
import kotlin.js.Promise

fun webpackDevServerStart(webpackOptions : dynamic, @Suppress("UNUSED_PARAMETER") devServerOptions: dynamic) : Promise<Unit> {

    val webpack = js("require('webpack')")
    @Suppress("UNUSED_VARIABLE") val Server = js("require('webpack-dev-server/lib/Server')")

    val compiler = try {
        webpack(webpackOptions)
    } catch (e : Throwable) {
        ColoredConsole.red(e.message ?: "Compiler meet error.")
        console.log(e)
        process.exit(1)
    }

    val server = try {
        js("new Server(devServerOptions, compiler)")
    } catch (e : Throwable) {
        ColoredConsole.red(e.message ?: "Dev server init meet error.")
        console.log(e)
        process.exit(1)
    }

    listOf("SIGINT", "SIGTERM").forEach {
        process.on(it) { _: Any -> server.stop { process.exit() }; Unit }
    }

    return server.start() as Promise<Unit>
}