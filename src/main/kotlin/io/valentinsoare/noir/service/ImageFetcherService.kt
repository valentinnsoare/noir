package io.valentinsoare.noir.service

interface ImageFetcherService {
    suspend fun getImage(url: String): ByteArray
}
