package webpack

import devServerConfiguration
import process
import webpack.common.Banner
import webpack.common.Env
import webpack.common.await
import webpack.common.jsObject
import webpack.devserver.webpackDevServerStart
import webpack.utils.ColoredConsole
import webpack.utils.OraSpinner
import webpack.utils.Rimraf
import webpackConfigurations

private val webpack = js("require('webpack')")

private suspend fun productionWebpackEntry(config: WebpackConfigContext) {
    val spinner = OraSpinner("Build for production...")
    spinner.start()

    Rimraf(config.env.project("dist/static"))

    val rawConfig = config.buildWebpackConfig()
    webpack(rawConfig) { error, stats ->
        spinner.stop()

        if (error != null) throw (error as Throwable)

        process.stdout.write((stats.toString(jsObject {
            it.colors = true
            it.modules = false
            it.children = false
            it.chunks = false
            it.chunkModules = false
        }) as String) + "\n\n")

        if (stats.hasErrors() as Boolean) {
            ColoredConsole.red("[*]  Build failed with errors.")
            process.exit(1)
        }

        ColoredConsole.cyan("[*]  Build complete.")
        ColoredConsole.yellow(
            """  
    [*]  Tip: built files are meant to be served over an HTTP server.
    [*]  Opening index.html over file:// won't work.
        """.trimIndent()
        )
    }
}

private suspend fun developmentWebpackEntry(config: WebpackConfigContext) {
    val rawConfig = config.buildWebpackConfig()
    val devServerConfig = config.devServerConfiguration
    ColoredConsole.green("[*]  Build for development")
    webpackDevServerStart(rawConfig, devServerConfig).await()
}

/**
 *
 * The main script entry
 *
 */
suspend fun main() {
    println(Banner.buildBanner())
    val selectedProfileName = when (val p = Env.processArgs.first()) {
        "build" -> "production"
        "serve", "dev" -> "development"
        else -> p
    }

    val configuration = webpackConfigurations.entries.firstOrNull {
        it.key == selectedProfileName
    }?.value?.invoke() ?: error("Profile with name '$selectedProfileName' not found. Please check your input or define it in 'buildscript.kt'.")

    when (selectedProfileName) {
        "development" -> developmentWebpackEntry(configuration)
        else -> productionWebpackEntry(configuration)
    }
}