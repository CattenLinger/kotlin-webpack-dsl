package webpack.utils

import webpack.common.jsObject
import webpack.common.nativePropertyWrapperOf
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object PortFinder {
    private val portFinder = js("require('portfinder')")

    class PortFinderOptions {
        private val internalObject = jsObject()
        var host by nativePropertyWrapperOf<String>(internalObject)
        var startPort by nativePropertyWrapperOf<Int>(internalObject)
        var port by nativePropertyWrapperOf<Int>(internalObject)
        var stopPort by nativePropertyWrapperOf<Int>(internalObject)

        fun build() = internalObject
    }

    suspend fun find(options : PortFinderOptions.() -> Unit) : Int = suspendCoroutine {
        val config = PortFinderOptions()
        options(config)
        portFinder.getPort(config) { error, port ->
            if(error !== null) it.resumeWithException(error as Exception)
            else it.resume(port as Int)
        }
        Unit
    }
}