package io.valentinsoare.noir.annotationandinterface

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class ProcessingScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class IOScope

interface WithLogging {
    val logging: KLogger
        get() = KotlinLogging.logger(this::class.java.name)

    fun configureVerboseLogging() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.getLogger("io.valentinsoare.noir").level = Level.DEBUG
        logging.debug { "Verbose logging enabled" }
    }
}
