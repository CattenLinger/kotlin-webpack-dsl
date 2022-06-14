package webpack

import webpack.common.jsObject
import webpack.common.nativePropertyWrapperOf

class WebpackOptimizationConfig(override val parent: WebpackDslContext) : WebpackDslContext, WebpackConfigStep {
    private val internal = jsObject()
    var chunkIds by nativePropertyWrapperOf<String>(internal)
    var moduleIds by nativePropertyWrapperOf<String>(internal)
    var minimize by nativePropertyWrapperOf<Boolean>(internal)

    override suspend fun invoke(rawObject: dynamic) {
        rawObject.optimization = internal
    }
}

@WebpackDsl
suspend fun WebpackConfigContext.optimization(block : suspend WebpackOptimizationConfig.() -> Unit)
= configStep(WebpackOptimizationConfig(this), block)