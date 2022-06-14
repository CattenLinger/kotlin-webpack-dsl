package webpack

import webpack.common.jsObject
import webpack.common.nativePropertyWrapperOf

/**
 * Webpack outputs configuration
 */
class WebpackOutputConfig(override val parent: WebpackDslContext) : WebpackDslContext, WebpackConfigStepAdapter() {
    private val internalObject = jsObject()

    var path: String? by nativePropertyWrapperOf(internalObject)
    var filename: String? by nativePropertyWrapperOf(internalObject)
    var publicPath: String? by nativePropertyWrapperOf(internalObject)

    init {
        subActions.add(WebpackConfigStep { webpack ->
            webpack.output = internalObject
        })
    }
}

@WebpackDsl
suspend fun WebpackConfigContext.output(block: suspend WebpackOutputConfig.() -> Unit) = configStep(WebpackOutputConfig(this), block)