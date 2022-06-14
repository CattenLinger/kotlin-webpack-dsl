package webpack.common

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