package webpack

import webpack.common.jsArray
import webpack.common.jsObject

@WebpackDsl
class WebpackLoader(private val supplier: () -> dynamic) {
    val rawConfig: dynamic
        get() = supplier()

    companion object {
        fun byName(loader: String, options: (dynamic.() -> Unit)? = null) = WebpackLoader {
            val obj = jsObject()
            obj.loader = loader
            options?.let { configurator ->
                obj.options = jsObject {
                    configurator(it)
                }
            }
            obj
        }
    }
}

fun Collection<WebpackLoader>.toRawConfig(): dynamic {
    val arr = jsArray()
    forEachIndexed { index, webpackLoader -> arr[index] = webpackLoader.rawConfig }
    return arr
}