package webpack

import webpack.common.jsObject

/**
 * Webpack entries configuration
 */
class WebpackEntryConfig(override val parent: WebpackDslContext) : WebpackDslContext, WebpackConfigStep {
    class WebPackEntry(val name: String, val config: (suspend () -> dynamic)? = null)

    private val entries = mutableListOf<WebPackEntry>()

    private fun ensureEntryListEmpty() {
        if (entries.isEmpty()) return
        error("Current webpack.entry config conflict with others. Please check your 'webpack { webpack.entry { ... } }' dsl.")
    }

    private fun ensureIsNotSingleEntryConfig(dslName: String) {
        if (entries.none { it.config == null }) return
        error("'$dslName' conflicts with 'name(name: String)'. Please check your 'webpack { webpack.entry { ... } }' dsl.")
    }

    @WebpackDsl
    fun name(name: String) {
        ensureEntryListEmpty()
        entries.add(WebPackEntry(name))
    }

    @WebpackDsl
    fun name(entry: Pair<String, String>) {
        ensureIsNotSingleEntryConfig("name(webpack.entry: Pair<String,String>)")
        this.entries.add(WebPackEntry(entry.first) { entry.second })
    }

    @WebpackDsl
    fun names(vararg entries: Pair<String, String>) {
        ensureIsNotSingleEntryConfig("names(vararg entries: Pair<String,String>)")
        this.entries.addAll(entries.map { WebPackEntry(it.first) { it.second } })
    }

    override suspend fun invoke(rawObject: dynamic) {
        if (entries.size == 1) {
            val entry = entries.firstOrNull { it.config == null }
            if (entry !== null) {
                rawObject.entry = entry.name
                return
            }
        }

        val obj = jsObject()
        entries.forEach { obj[it.name] = it.config?.invoke() ?: null }
        rawObject.entry = obj
    }
}

@WebpackDsl
suspend fun WebpackConfigContext.entry(block: suspend WebpackEntryConfig.() -> Unit) = configStep(WebpackEntryConfig(this), block)