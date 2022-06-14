package webpack

import process
import path.path

/**
 * Environment helper
 */
class WebpackConfigEnvironment(private val projectRoot: String) {
    class PathConvenient {
        fun join(vararg string: String): String {
            return path.posix.join(*string)
        }

        fun resolve(vararg string: String): String {
            return path.resolve(*string)
        }
    }

    val path = PathConvenient()

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