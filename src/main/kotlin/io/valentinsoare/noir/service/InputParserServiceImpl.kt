package io.valentinsoare.noir.service

import io.valentinsoare.noir.annotationandinterface.WithLogging
import org.springframework.stereotype.Service
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class InputParserServiceImpl : InputParserService, WithLogging {

    companion object {
        private val SUPPORTED_PROTOCOLS = setOf("http", "https")
    }

    override fun validateImageUrl(urlString: String): URL {
        logging.debug { "Validating URL: $urlString" }

        if (urlString.isBlank()) {
            throw IllegalArgumentException("URL cannot be empty")
        }

        val url = try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("Invalid URL format: ${e.message}")
        }

        if (url.protocol !in SUPPORTED_PROTOCOLS) {
            throw IllegalArgumentException(
                "Unsupported protocol '${url.protocol}'. Supported: ${SUPPORTED_PROTOCOLS.joinToString()}"
            )
        }

        if (url.host.isNullOrBlank()) {
            throw IllegalArgumentException("URL must have a valid host")
        }

        logging.info { "URL validated successfully: $urlString" }
        return url
    }

    override fun isValidImageUrl(urlString: String): Boolean {
        return try {
            validateImageUrl(urlString)
            true
        } catch (e: IllegalArgumentException) {
            logging.debug { "URL validation failed: ${e.message}" }
            false
        }
    }

    override fun generateOutputFilename(url: String, customName: String?): String {
        if (!customName.isNullOrBlank()) {
            val name = if (customName.endsWith(".png")) customName else "$customName.png"

            logging.debug { "Using custom output filename: $name" }
            return name
        }

        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "processed_image_$timestamp.png"

        logging.debug { "Generated output filename: $filename" }
        return filename
    }
}
