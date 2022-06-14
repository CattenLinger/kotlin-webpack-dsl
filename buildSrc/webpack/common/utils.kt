package webpack.common

import kotlin.coroutines.*
import kotlin.js.Promise
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Create an empty javascript object
 */
fun jsObject(): dynamic = js("({})")

fun jsObject(block : (dynamic) -> Unit) : dynamic {
    val obj = jsObject()
    block(obj)
    return obj
}

/**
 * Create an empty javascript array
 */
fun jsArray(): dynamic = js("([])")

/**
 * Create a javascript regex by given pattern
 */
fun jsRegex(@Suppress("UNUSED_PARAMETER") pattern: String): dynamic = js("new RegExp(pattern)")

@Suppress("UNUSED_VARIABLE")
fun Regex.nativePattern(): dynamic {
    val that = this
    return js("(that.nativePattern_0)")
}

/**
 * Invoke Object.assign() function
 */
@Suppress("UNUSED_PARAMETER")
fun jsAssign(target: dynamic, obj: dynamic): dynamic {
    return js("Object.assign(target, obj)")
}

fun jsAssign(target: dynamic, vararg objs: dynamic): dynamic {
    for (obj in objs) jsAssign(target, obj)
    return target
}

/**
 * Create a common.promise using given suspend [block]
 *
 * @param coroutineContext change the coroutine context to given one
 */
fun <T> promise(coroutineContext: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): Promise<Result<T>> {
    return Promise { resolve, reject ->
        block.startCoroutine(object : Continuation<T> {
            override val context: CoroutineContext get() = coroutineContext
            override fun resumeWith(result: Result<T>) {
                if (result.isFailure) reject(result.exceptionOrNull() ?: Exception("Coroutine failed."))
                else resolve(result)
            }
        })
    }
}

/**
 * Await a common.promise
 */
suspend fun <T> Promise<T>.await(): T = suspendCoroutine { coroutine ->
    then {
        coroutine.resume(it)
    }.catch {
        coroutine.resumeWithException(it)
    }
}

/**
 * Create a native property wrapper of a js subject
 */
fun <T> nativePropertyWrapperOf(d: dynamic) = object : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = d[property.name] as T?
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        d[property.name] = value
    }
}

/**
 * Convert a map to pure js object
 */
fun Map<String, *>.toJsObject(): dynamic {
    val obj = jsObject()
    for ((key, value) in this) obj[key] = value
    return obj
}

/**
 * Convert a list of pairs to js object
 */
fun Collection<Pair<*, *>>.toJsObject(): dynamic {
    val obj = jsObject()
    for ((first, second) in this) obj[first] = second
    return obj
}

/**
 * Convert an array of pairs to js object
 */
fun Array<out Pair<*, *>>.toJsObject(): dynamic {
    val obj = jsObject()
    for ((first, second) in this) obj[first] = second
    return obj
}

/**
 * Convert a collection to js array
 */
fun Collection<*>.toJsArray(): dynamic {
    val array = jsArray()
    forEachIndexed { index, item -> array[index] = item }
    return array
}