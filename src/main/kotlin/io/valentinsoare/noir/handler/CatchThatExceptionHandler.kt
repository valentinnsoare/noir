package io.valentinsoare.noir.handler

import kotlinx.coroutines.CoroutineExceptionHandler

interface CatchThatExceptionHandler {
    fun getExceptionHandler(): CoroutineExceptionHandler
}
