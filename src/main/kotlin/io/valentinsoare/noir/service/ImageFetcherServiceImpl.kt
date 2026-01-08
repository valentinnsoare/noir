package io.valentinsoare.noir.service

import io.valentinsoare.noir.utils.WithLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class ImageFetcherServiceImpl(
    private val webClient: WebClient
): ImageFetcherService, WithLogging {

    override suspend fun getImage(url: String): ByteArray {
        logging.debug { "Fetching image from URL: $url" }
        val awaitSingle = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono<ByteArray>()
            .awaitSingle()

        logging.debug { "Image fetched successfully" }
        return awaitSingle
    }
}
