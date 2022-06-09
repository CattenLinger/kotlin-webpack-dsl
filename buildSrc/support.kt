import fs.readdir
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import path.path;
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private fun createJsObject(): dynamic = js("({})")
private fun createJsArray(): dynamic = js("([])")

fun <T> nativePropertyOf(d: dynamic) = object : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return d[property.name] as T?
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        d[property.name] = value
    }
}

private fun Map<String, *>.toJsonObject(): dynamic {
    val obj = createJsObject()
    for ((key, value) in this) obj[key] = value
    return obj
}

private fun Collection<Pair<*, *>>.toJsonObject(): dynamic {
    val obj = createJsObject()
    for ((first, second) in this) obj[first] = second
    return obj
}

private fun Array<out Pair<*, *>>.toJsonObject(): dynamic {
    val obj = createJsObject()
    for ((first, second) in this) obj[first] = second
    return obj
}

private fun Collection<*>.toJsonArray(): dynamic {
    val array = createJsArray()
    forEachIndexed { index, item -> array[index] = item }
    return array
}

/* Start Webpack functionalities */

@DslMarker
annotation class WebpackDsl

@DslMarker
annotation class WebpackRawAccessDsl

@Suppress("ObjectPropertyName")
private val _webpack: dynamic = js("require('webpack')")

interface WebpackConfigAction {
    suspend operator fun invoke(webpackConfig: dynamic)

    companion object {
        operator fun invoke(block: suspend (dynamic) -> Unit) = object : WebpackConfigAction {
            override suspend fun invoke(webpackConfig: dynamic) {
                block(webpackConfig)
            }
        }
    }
}

open class WebpackConfigActionAdapter(
    protected val subActions: MutableList<WebpackConfigAction> = mutableListOf()
) : WebpackConfigAction {
    override suspend fun invoke(webpackConfig: dynamic) = subActions.forEach { it(webpackConfig) }
}

/*  Webpack config dsl */

/**
 * Webpack entries configuration
 */
class WebpackEntryConfig : WebpackConfigActionAdapter() {
    class WebPackEntry(val name: String, val config: (suspend () -> dynamic)? = null)

    private val entries = mutableListOf<WebPackEntry>()

    private fun ensureEntryListEmpty() {
        if (entries.isEmpty()) return
        error("Current entry config conflict with others. Please check your 'webpack { entry { ... } }' dsl.")
    }

    private fun ensureIsNotSingleEntryConfig(dslName : String) {
        if(entries.none { it.config == null }) return
        error("'$dslName' conflicts with 'name(name: String)'. Please check your 'webpack { entry { ... } }' dsl.")
    }

    @WebpackDsl
    fun name(name: String) {
        ensureEntryListEmpty()
        entries.add(WebPackEntry(name))
    }

    @WebpackDsl
    fun name(entry: Pair<String, String>) {
        ensureIsNotSingleEntryConfig("name(entry: Pair<String,String>)")
        this.entries.add(WebPackEntry(entry.first) { entry.second })
    }

    @WebpackDsl
    fun names(vararg entries: Pair<String, String>) {
        ensureIsNotSingleEntryConfig("names(vararg entries: Pair<String,String>)")
        this.entries.addAll(entries.map { WebPackEntry(it.first) { it.second } })
    }

    override suspend fun invoke(webpackConfig: dynamic) {
        if (entries.size == 1) {
            val entry = entries.firstOrNull { it.config == null }
            if(entry !== null) {
                webpackConfig.entry = entry.name
                return
            }
        }

        val obj = createJsObject()
        entries.forEach { obj[it.name] = it.config?.invoke() ?: null }
        webpackConfig.entry = obj
    }
}

@WebpackDsl
suspend fun WebpackConfigurationContext.entry(block: suspend WebpackEntryConfig.() -> Unit) = applyConfigAction(WebpackEntryConfig(), block)

/**
 * Webpack outputs configuration
 */
class WebpackOutputConfig : WebpackConfigActionAdapter() {
    private val internalObject = createJsObject()

    var path: String? by nativePropertyOf(internalObject)
    var filename: String? by nativePropertyOf(internalObject)
    var publicPath: String? by nativePropertyOf(internalObject)

    init {
        subActions.add(WebpackConfigAction { webpack ->
            webpack.output = internalObject
        })
    }
}

@WebpackDsl
suspend fun WebpackConfigurationContext.output(block: suspend WebpackOutputConfig.() -> Unit) = applyConfigAction(WebpackOutputConfig(), block)

/**
 * Webpack resolving configuration
 */
class WebpackResolveConfig : WebpackConfigActionAdapter() {
    var extensions: List<String>? = null
    var alias: Map<String, String>? = null

    init {
        subActions.add(WebpackConfigAction { webpack ->
            val obj = createJsObject()
            extensions?.let { obj.extensions = it.toJsonArray() }
            alias?.let { obj.alias = it.toJsonObject() }
            webpack.resolve = obj
        })
    }
}

@WebpackDsl
suspend fun WebpackConfigurationContext.resolve(block: suspend WebpackResolveConfig.() -> Unit) = applyConfigAction(WebpackResolveConfig(), block)

/**
 * Webpack modules configuration
 */
class WebpackModuleConfig : WebpackConfigActionAdapter() {
    private val rules = mutableListOf<Rule>()

    class Rule {
        internal val internalObject = createJsObject()
        var loader: String? by nativePropertyOf(internalObject)
        internal val options = createJsArray()

        fun test(string: String) {
            internalObject["test"] = string
        }

        fun test(@Suppress("UNUSED_PARAMETER") regex: Regex) {
            internalObject["test"] = js("(regex.nativePattern_0)")
        }

        fun test(validator: suspend (String) -> Boolean) {
            internalObject["test"] = validator
        }

        fun include(vararg names: String) {
            internalObject.include = names
        }

        @WebpackRawAccessDsl
        suspend fun options(block: suspend dynamic.() -> Unit) = block(options)
    }

    @WebpackDsl
    suspend fun rule(loader: String, test: Regex, block: (suspend Rule.() -> Unit)? = null) {
        val rule = Rule()
        rule.test(test)
        rule.loader = loader
        rules.add(Rule().also { block?.invoke(rule) })
    }

    @WebpackDsl
    suspend fun rule(block: suspend Rule.() -> Unit) {
        rules.add(Rule().also { block(it) })
    }

    override suspend fun invoke(webpackConfig: dynamic) {
        val config = createJsObject()
        val rules = this.rules.map {
            it.internalObject.options = it.options
            it.internalObject
        }.toJsonArray()
        config.rules = rules
        webpackConfig.module = config
    }
}

@WebpackDsl
suspend fun WebpackConfigurationContext.module(block: suspend WebpackModuleConfig.() -> Unit) = applyConfigAction(WebpackModuleConfig(), block)

/**
 * Webpack node configuration
 */

@WebpackRawAccessDsl
suspend fun WebpackConfigurationContext.node(block: suspend dynamic.() -> Unit) = config {
    val config = createJsObject()
    block(config)
    it.node = config
}

/**
 * The webpack configuration context
 */
class WebpackConfigurationContext(val projectRoot: String) {
    private val actions = mutableListOf<WebpackConfigAction>()
    fun config(block: suspend (dynamic) -> Unit) = actions.add(WebpackConfigAction(block))
    internal suspend fun <T : WebpackConfigAction> applyConfigAction(action: T, config: suspend T.() -> Unit) {
        actions.add(action.also { config(it) })
    }

    val env = WebpackConfigEnvironment(projectRoot)

    @WebpackDsl
    fun context(string: String) = config { it.context = string }

    class WebpackConfigEnvironment(private val projectRoot: String) {
        val processEnv = process.env.asDynamic()

        val nodeEnv: String?
            get() = processEnv.NODE_ENV as String?

        val isProduction: Boolean
            get() = nodeEnv == "production"

        val isDevelopment: Boolean
            get() = nodeEnv == "development"

        fun project(string: String): String {
            return path.resolve(projectRoot, string)
        }

        fun nodeModules(string: String): String {
            return path.resolve("$projectRoot/node_modules/", string)
        }
    }

    /**
     * A developer tool to enhance debugging.
     *
     * Origin definition
     * devtool?: string | false;
     *
     * @param name false | eval | [inline-|hidden-|eval-][nosources-][cheap-[module-]]source-map
     */
    @WebpackDsl
    fun developmentTool(name: String) = config {
        it.devtool = name
    }

    /**
     * Set devtool to false
     * @see developmentTool
     */
    @WebpackDsl
    fun noDevelopmentTool() = config {
        it.devtool = false
    }

    suspend fun toWebpackConfig(): dynamic {
        val obj = createJsObject()
        actions.forEach { it(obj) }
        return obj
    }
}

/**
 * The webpack dsl
 */


@WebpackDsl
@Suppress("FunctionName")
suspend fun WebpackConfig(block: suspend WebpackConfigurationContext.() -> Unit): WebpackConfigurationContext {
    val context = WebpackConfigurationContext(
        Env.findBuildRoot() ?: error("Could not find build root! Please ensure your 'package.json' exists!")
    )
    block.invoke(context)
    return context
}

/* End webpack functionalities */

object Files {
    suspend fun readDir(path: String) = suspendCoroutine<Array<String>> {
        readdir(path) { error, array ->
            if (error != null) it.resumeWith(Result.failure(error))
            else it.resume(array)
        }
    }
}

object Env {
    val BuildScriptLocation = __filename
    val BuildScriptDir = __dirname
    val processArgs = process.argv.drop(2)

    suspend fun findBuildRoot(): String? {
        var parentPath = path.resolve("$BuildScriptDir/..")
        var count = 4
        do {
            val files = Files.readDir(parentPath)
            if (files.contains("package.json")) return parentPath
            parentPath = path.resolve("$parentPath/..")
            count--
        } while (count > 0)
        return null
    }
}

object Banner {
    fun buildBanner() = """
###############################################################################################
# Kotlin/JS Buildscript
###############################################################################################
# Buildscript Directory: ${Env.BuildScriptLocation}
# Buildscript Location : ${Env.BuildScriptDir}
###############################################################################################
# Process args:
# ${Env.processArgs.joinToString(", ") { "'$it'" }}
###############################################################################################
""".trimIndent()

}

suspend fun main() {
    println(Banner.buildBanner())
    val buildRoot = Env.findBuildRoot() ?: return println("Build root not found !")
    println("Build root: $buildRoot")

    @Suppress("UNUSED_VARIABLE") val config = webpackBaseConfig().toWebpackConfig()
    kotlin.js.console.log(js("JSON.stringify(config)"))
}