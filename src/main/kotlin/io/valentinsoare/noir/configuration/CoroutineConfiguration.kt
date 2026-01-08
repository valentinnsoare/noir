package io.valentinsoare.noir.configuration

import io.valentinsoare.noir.annotationandinterface.IOScope
import io.valentinsoare.noir.annotationandinterface.ProcessingScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfiguration (
    private val exceptionHandler: CoroutineExceptionHandler
) {

    @Bean
    @ProcessingScope
    fun processingScope(): CoroutineScope =
        CoroutineScope(Dispatchers.Default + exceptionHandler)

    @Bean
    @IOScope
    fun ioScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO + exceptionHandler)
}
