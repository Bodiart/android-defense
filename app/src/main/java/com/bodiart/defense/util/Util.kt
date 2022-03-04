package com.bodiart.defense.util

/**
 * Helper function that creates [Lazy] instance using [lazy] with [LazyThreadSafetyMode#NONE].
 *
 * @param T Type of lazy value
 *
 * @param initializer Function to initialize lazy value
 *
 * @return [Lazy] instance without usage of synchronization locks
 */
fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

@SinceKotlin("1.3")
public inline fun <R> mRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        log("RunCatching", "Fail", e)
        Result.failure(e)
    }
}