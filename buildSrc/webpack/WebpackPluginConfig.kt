package webpack

import webpack.common.jsArray
import webpack.common.jsObject
import webpack.common.nativePropertyWrapperOf

/**
 * Webpack plugin configuration
 */
@Suppress("FunctionName")
class WebpackPluginConfig(override val parent: WebpackDslContext) : WebpackDslContext, WebpackConfigStep {

    val plugins = mutableListOf<() -> dynamic>()

    @Suppress("ObjectPropertyName", "unused")
    private val _webpack = js("require('webpack')")

    fun WebpackDefinePlugin(config: (dynamic.() -> Unit)? = null) = plugins.add {
        @Suppress("UNUSED_VARIABLE") val webpack = this._webpack
        if (config !== null) {
            @Suppress("UNUSED_VARIABLE") val obj = jsObject(config)
            js("new webpack.DefinePlugin(obj)")
        } else {
            js("new webpack.DefinePlugin()")
        }
    }

    fun WebpackHotModuleReplacementPlugin(config: (dynamic.() -> Unit)? = null) = plugins.add {
        @Suppress("UNUSED_VARIABLE") val webpack = this._webpack
        if (config !== null) {
            @Suppress("UNUSED_VARIABLE") val obj = jsObject(config)
            js("new webpack.HotModuleReplacementPlugin(obj)")
        } else {
            js("new webpack.HotModuleReplacementPlugin()")
        }
    }

    fun WebpackModuleConcatenationPlugin() = plugins.add {
        @Suppress("UNUSED_VARIABLE") val webpack = this._webpack
        js("new webpack.optimize.ModuleConcatenationPlugin()")
    }

    //new webpack.optimize.CommonsChunkPlugin

    class CommonsChunkPluginConfig {
        internal val internalObject = jsObject()
        var name: String? by nativePropertyWrapperOf(internalObject)
        var async: String? by nativePropertyWrapperOf(internalObject)
        var children: Boolean? by nativePropertyWrapperOf(internalObject)

        fun miniChunks(value: Int) {
            internalObject.miniChunks = value
        }

        fun miniChunks(calculator: (dynamic) -> Boolean) {
            internalObject.miniChunks = calculator
        }

        fun noMiniChunks() {
            internalObject.miniChunks = js("Infinity")
        }
    }

    fun WebpackOptimizeCommonsChunkPlugin(config: CommonsChunkPluginConfig.() -> Unit) = plugins.add {
        val plugin = CommonsChunkPluginConfig()
        plugin.apply(config)
        plugin.internalObject
    }

    fun WebpackOptimizeModuleConcatenationPlugin() = plugins.add {
        @Suppress("UNUSED_VARIABLE") val webpack = this._webpack
        js("new webpack.optimize.ModuleConcatenationPlugin()")
    }

    fun WebpackNoEmitOnErrorPlugin() = plugins.add {
        @Suppress("UNUSED_VARIABLE") val webpack = this._webpack
        js("new webpack.NoEmitOnErrorsPlugin()")
    }

    override suspend fun invoke(rawObject: dynamic) {
        val arr = jsArray()
        plugins.forEachIndexed { index, function -> arr[index] = function() }
        rawObject.plugins = arr
    }
}

@WebpackDsl
suspend fun WebpackConfigContext.plugins(block: suspend WebpackPluginConfig.() -> Unit) =
    configStep(WebpackPluginConfig(this), block)