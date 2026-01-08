package io.valentinsoare.noir.configuration

import io.valentinsoare.noir.annotationandinterface.WithLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration: WithLogging {

    @Bean
    fun webClient(): WebClient {
        logging.debug { "Build webClient with custom setup" }
        val webClient = WebClient.builder()
             .codecs {
                 it.defaultCodecs().maxInMemorySize(3 * 1024 * 1024)
             }
             .build()

        logging.debug { "WebClient built successfully" }
        return webClient
    }
}
