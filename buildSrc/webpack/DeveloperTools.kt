package webpack

/**
 * Developer tools
 */
enum class DeveloperTools(private val config: (dynamic) -> Unit) : WebpackConfigStep {
    Disable({ it.devtool = false }),
    Eval({ it.devtool = "eval" }),
    InlineSourceMap({ it.devtool = "inline-source-map" }),
    HiddenSourceMap({ it.devtool = "hidden-source-map" }),
    EvalSourceMap({ it.devtool = "eval-source-map" }),
    NoSourcesSourceMap({ it.devtool = "nosources-source-map" }),
    CheapSourceMap({ it.devtool = "cheap-source-map" }),
    CheapModelSourceMap({ it.devtool = "cheap-module-source-map" }),
    ;

    override suspend fun invoke(rawObject: dynamic) = config(rawObject)
}