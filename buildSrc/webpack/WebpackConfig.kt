package webpack

import webpack.common.Env
import webpack.common.jsObject

/* Start Webpack functionalities */

@DslMarker
annotation class WebpackDsl

@DslMarker
annotation class WebpackRawAccessDsl

@Suppress("ObjectPropertyName", "unused")
private val _webpackMerge = js("require('webpack-merge')")

interface WebpackDslContext {
    val parent: WebpackDslContext

    val env: WebpackConfigEnvironment
        get() = parent.env
}

interface WebpackConfigStep {
    suspend operator fun invoke(rawObject: dynamic)

    companion object {
        operator fun invoke(block: suspend (dynamic) -> Unit) = object : WebpackConfigStep {
            override suspend fun invoke(rawObject: dynamic) {
                block(rawObject)
            }
        }
    }
}

open class WebpackConfigStepAdapter(protected val subActions: MutableList<WebpackConfigStep> = mutableListOf()) : WebpackConfigStep {
    override suspend fun invoke(rawObject: dynamic) = subActions.forEach { it(rawObject) }
}

/*  Webpack config dsl */

/**
 *
 *
 *
 * The webpack configuration context
 *
 *
 */
class WebpackConfigContext(internal val projectRoot: String, internal val builder : suspend WebpackConfigContext.() -> Unit) : WebpackDslContext {
    private val steps = mutableListOf<WebpackConfigStep>()

    fun addStep(block: suspend (dynamic) -> Unit) = steps.add(WebpackConfigStep(block))

    internal suspend fun <T : WebpackConfigStep> configStep(action: T, config: suspend T.() -> Unit) {
        steps.add(action.also { config(it) })
    }

    override val env = WebpackConfigEnvironment(projectRoot)
    override val parent = this

    @WebpackDsl
    fun context(string: String) = addStep { it.context = string }

    @WebpackDsl
    fun mode(string: String) = addStep { it.mode = string }

    /**
     * A developer tool to enhance debugging.
     *
     * Origin definition
     * devtool?: string | false;
     *
     * @param name false | eval | [inline-|hidden-|eval-][nosources-][cheap-[webpack.module-]]source-map
     */
    @WebpackDsl
    fun developerTool(name: String) = addStep { it.devtool = name }

    @WebpackDsl
    fun developerTool(tool: DeveloperTools) = steps.add(tool)

    /**
     * Set devtool to false
     * @see developerTool
     */
    @WebpackDsl
    fun noDeveloperTool() = steps.add(DeveloperTools.Disable)

    suspend fun buildWebpackConfig(): dynamic {
        builder(this)
        val obj = jsObject()
        steps.forEach { it(obj) }
        return obj
    }

    @Suppress("UNUSED_VARIABLE")
    @WebpackDsl
    fun merge(block: suspend WebpackConfigContext.() -> Unit) : suspend () -> dynamic = {
        val additional = WebpackConfig(block)()
        val base = this.buildWebpackConfig()
        val merge = _webpackMerge
        js("merge(base, additional)")
    }
}

@WebpackDsl
fun (suspend  () -> WebpackConfigContext).merge(block : suspend WebpackConfigContext.() -> Unit) : (suspend () -> WebpackConfigContext) {
    val base = this
    return suspend {
        val baseConfig = base()
        WebpackConfigContext(baseConfig.projectRoot) {
            baseConfig.builder(this)
            block(this)
        }
    }
}

/**
 *
 *
 *
 * The webpack dsl
 *
 *
 *
 */
@WebpackDsl
@Suppress("FunctionName")
fun WebpackConfig(block: suspend WebpackConfigContext.() -> Unit) : (suspend  () -> WebpackConfigContext) = {
    WebpackConfigContext(
        Env.findBuildRoot() ?: error("Could not find build root! Please ensure your 'package.json' exists!"),
        block
    )
}

/* End webpack functionalities */