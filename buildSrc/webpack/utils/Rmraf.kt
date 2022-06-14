package webpack.utils

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val rimraf = js("require('rimraf')")

suspend fun Rimraf(path : String) : Unit = suspendCoroutine {
    rimraf(path) { error ->
        val casedError = error.unsafeCast<Throwable?>()
        if(casedError == null) it.resume(Unit)
        else it.resumeWithException(casedError)
    }
    Unit
}