package webpack

import webpack.common.jsObject
import webpack.common.nativePropertyWrapperOf
import webpack.common.toJsArray


fun WebpackPluginConfig.MiniCssExtractPlugin() = plugins.add {
    @Suppress("UNUSED_VARIABLE") val mcep = js("require('mini-css-extract-plugin')")
    js("new mcep()")
}

/**
 *
 * HTML Webpack Plugin
 *
 */
class HtmlWebpackPluginConfiguration {
    private val internal = jsObject()
    var filename by nativePropertyWrapperOf<String>(internal)
    var template by nativePropertyWrapperOf<String>(internal)
    var inject by nativePropertyWrapperOf<Boolean>(internal)
    var chunksSortMode by nativePropertyWrapperOf<String>(internal)

    class MinifyOptions {
        private val internal = jsObject()
        var removeComments by nativePropertyWrapperOf<Boolean>(internal)
        var collapseWhitespace by nativePropertyWrapperOf<Boolean>(internal)
        var removeAttributeQuotes by nativePropertyWrapperOf<Boolean>(internal)
        fun build() = internal
    }

    fun minifyOptions(block : MinifyOptions.() -> Unit) {
        val conf = MinifyOptions()
        block(conf)
        internal.minify = conf.build()
    }

    @Suppress("UNUSED_VARIABLE")
    fun build(): dynamic {
        val htmlWebpackPlugin = js("require('html-webpack-plugin')")
        val args = internal
        return js("new htmlWebpackPlugin(args)")
    }
}

fun WebpackPluginConfig.HtmlWebpackPlugin(config: HtmlWebpackPluginConfiguration.() -> Unit) = plugins.add {
    val conf = HtmlWebpackPluginConfiguration()
    config(conf)
    conf.build()
}

/**
 * Copy webpack plugin
 */
class CopyWebpackPluginConfiguration {

    class Pattern(val from: String, val to: String, val filter : ((String) -> Boolean)?)

    private val patterns = mutableListOf<Pattern>()

    fun copy(from: String, to: String, filter: ((String) -> Boolean)? = null) {
        patterns.add(Pattern(from, to, filter))
    }

    @Suppress("UNUSED_VARIABLE")
    fun build(): dynamic {
        val cwp = js("require('copy-webpack-plugin')")
        val obj = jsObject()
        obj.patterns = patterns.map { p -> jsObject { it["from"] = p.from; it["to"] = p.to; if (p.filter != null) it.filter = p.filter; } }.toJsArray()
        return js("new cwp(obj)")
    }
}

fun WebpackPluginConfig.CopyWebpackPlugin(config: CopyWebpackPluginConfiguration.() -> Unit) = plugins.add {
    val conf = CopyWebpackPluginConfiguration()
    config(conf)
    conf.build()
}

