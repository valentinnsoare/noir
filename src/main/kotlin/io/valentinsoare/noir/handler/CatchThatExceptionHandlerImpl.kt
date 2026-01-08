package io.valentinsoare.noir.handler

import io.valentinsoare.noir.utils.WithLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class CatchThatExceptionHandlerImpl: CatchThatExceptionHandler, WithLogging {

    @Bean
    override fun getExceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { context, exception ->
            val coroutineName = context[CoroutineName]?.name ?: "unnamed"
            println("Uncaught exception in coroutine [$coroutineName]. Exception: ${exception.message}" )
        }
}
