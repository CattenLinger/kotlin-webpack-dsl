import webpack.WebpackLoader
import webpack.WebpackModuleConfig
import webpack.common.toJsArray
import webpack.toRawConfig

@Suppress("UNUSED_ANONYMOUS_PARAMETER", "UNUSED_VARIABLE")
fun styleLoader(
    name : String? = null,
    sourceMap : Boolean,
    useExtractTextPlugin: Boolean,
    usePostCss : Boolean,
    options : (dynamic.() -> Unit)? = null
) : WebpackLoader {

    val list = mutableListOf(WebpackLoader.byName("css-loader") { this["sourceMap"] = sourceMap })
    if(usePostCss) list.add(WebpackLoader.byName("postcss-loader") { this["sourceMap"] = sourceMap })
    val vueLoader = WebpackLoader.byName("vue-style-loader")
    name?.let { list.add(WebpackLoader.byName("${name}-loader", options)) }

    return if(useExtractTextPlugin) {
        val extractPlugin = js("require('mini-css-extract-plugin')")
        val list1 = mutableListOf<dynamic>()
        list1.add(js("extractPlugin.loader"))
        list1.addAll(list.map { it.rawConfig })
        WebpackLoader { list1.toJsArray() }
    } else WebpackLoader { (list + vueLoader).toRawConfig() }
}

fun WebpackModuleConfig.useStylesheetLoaders(sourceMap: Boolean = true, extractCss: Boolean = true, usePostCss: Boolean = true) {
    rule(Regex("\\.(css|postcss)\$"), styleLoader(null, sourceMap, extractCss, usePostCss))
    rule(Regex("\\.(less)\$"), styleLoader("less", sourceMap, extractCss, usePostCss))
    rule(Regex("\\.(sass)"), styleLoader("sass", sourceMap, extractCss, usePostCss) {
        indentedSyntax = true
    })
    rule(Regex("\\.(scss)\$"), styleLoader("sass", sourceMap, extractCss, usePostCss))
    rule(Regex("\\.(stylus|styl)\$"), styleLoader("stylus", sourceMap, extractCss, usePostCss))
}