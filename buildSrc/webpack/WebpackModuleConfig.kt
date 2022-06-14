package webpack

import webpack.common.jsObject
import webpack.common.nativePattern
import webpack.common.nativePropertyWrapperOf
import webpack.common.toJsArray

/**
 * Webpack modules configuration
 */
class WebpackModuleConfig(override val parent: WebpackDslContext) : WebpackDslContext, WebpackConfigStep {
    private val rules = mutableListOf<Rule>()

    class Rule {
        internal val internalObject = jsObject()
        var loader: String? by nativePropertyWrapperOf(internalObject)
        internal var options: dynamic = null
        internal var use: dynamic = null

        fun test(string: String) {
            internalObject["test"] = string
        }

        fun test(@Suppress("UNUSED_PARAMETER") regex: Regex) {
            internalObject["test"] = regex.nativePattern()
        }

        fun test(validator: suspend (String) -> Boolean) {
            internalObject["test"] = validator
        }

        fun include(vararg names: String) {
            internalObject.include = names
        }

        @WebpackRawAccessDsl
        suspend fun options(block: suspend dynamic.() -> Unit) {
            options = jsObject()
            block(options)
        }
    }

    @WebpackDsl
    suspend fun rule(loader: String, test: Regex, block: (suspend Rule.() -> Unit)? = null) {
        val rule = Rule()
        rule.test(test)
        rule.loader = loader
        rules.add(Rule().also { block?.invoke(rule) })
    }

    @WebpackDsl
    fun rule(test: Regex, use: List<WebpackLoader>) {
        val rule = Rule()
        rule.test(test)
        rule.use = use.toRawConfig()
        rules.add(rule)
    }

    @WebpackDsl
    fun rule(test: Regex, use: WebpackLoader) {
        val rule = Rule()
        rule.test(test)
        rule.internalObject.use = use.rawConfig
        rules.add(rule)
    }

    @WebpackDsl
    suspend fun rule(block: suspend Rule.() -> Unit) {
        rules.add(Rule().also { block(it) })
    }

    override suspend fun invoke(rawObject: dynamic) {
        val config = jsObject()
        val rules = this.rules.map {
            if (it.options != null) it.internalObject.options = it.options
            if (it.use !== null) it.internalObject.use = it.use
            it.internalObject
        }.toJsArray()
        config.rules = rules
        rawObject.module = config
    }
}

@WebpackDsl
suspend fun WebpackConfigContext.module(block: suspend WebpackModuleConfig.() -> Unit) = configStep(WebpackModuleConfig(this), block)