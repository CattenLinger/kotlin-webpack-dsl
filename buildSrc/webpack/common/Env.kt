package webpack.common

import __dirname
import __filename
import process
import path.path

object Env {
    val BuildScriptLocation = __filename
    val BuildScriptDir = __dirname
    val processArgs = process.argv.drop(2)

    suspend fun findBuildRoot(): String? {
        var parentPath = path.resolve(BuildScriptDir, "..")
        var count = 4
        while(count > 0) {
            val files = Files.readDir(parentPath)
            if (files.contains("package.json")) return parentPath
            parentPath = path.resolve(parentPath, "..")
            count--
        }
        return null
    }
}