package com.amali.annotably.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Utility functions for coroutines
 */

/**
 * Execute a suspend function on IO dispatcher
 */
suspend fun <T> ioDispatcher(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO) {
        block()
    }
}

/**
 * Execute a suspend function on Main dispatcher
 */
suspend fun <T> mainDispatcher(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Main) {
        block()
    }
}

/**
 * Launch a coroutine on IO dispatcher
 */
fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.IO) {
        block()
    }
}

/**
 * Launch a coroutine on Main dispatcher
 */
fun CoroutineScope.launchMain(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.Main) {
        block()
    }
}
