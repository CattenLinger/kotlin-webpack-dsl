package webpack.common

import fs.readdir
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import path.path

object Files {
    suspend fun readDir(path: String) = suspendCoroutine<Array<String>> {
        readdir(path) { error, array -> if (error != null) it.resumeWithException(error) else it.resume(array) }
    }

    val pathDelimiter : String = path.sep
}