package io.valentinsoare.noir.service

interface ImageProcessingService {
    suspend fun processImage(imageBytes: ByteArray): ByteArray
}
