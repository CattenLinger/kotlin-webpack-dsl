package webpack

import webpack.common.jsObject
import webpack.common.toJsArray
import webpack.common.toJsObject

/**
 * Webpack resolving configuration
 */
class WebpackResolveConfig(override val parent: WebpackDslContext) : WebpackDslContext, WebpackConfigStepAdapter() {
    var extensions: List<String>? = null
    var alias: Map<String, String>? = null

    init {
        subActions.add(WebpackConfigStep { webpack ->
            val obj = jsObject()
            extensions?.let { obj.extensions = it.toJsArray() }
            alias?.let { obj.alias = it.toJsObject() }
            webpack.resolve = obj
        })
    }
}

@WebpackDsl
suspend fun WebpackConfigContext.resolve(block: suspend WebpackResolveConfig.() -> Unit) = configStep(WebpackResolveConfig(this), block)