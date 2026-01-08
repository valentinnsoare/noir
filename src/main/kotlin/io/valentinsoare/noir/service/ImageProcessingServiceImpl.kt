package io.valentinsoare.noir.service

import io.valentinsoare.noir.annotationandinterface.WithLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class ImageProcessingServiceImpl : ImageProcessingService, WithLogging {
    override suspend fun processImage(imageBytes: ByteArray): ByteArray {
        logging.debug { "Processing image" }

        val inputStream = ByteArrayInputStream(imageBytes)
        val originalImage: BufferedImage = ImageIO.read(inputStream)

        val grayColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY)
        val colorConvertOp = ColorConvertOp(grayColorSpace, null)
        val grayscaleImage: BufferedImage = colorConvertOp.filter(originalImage, null)

        val outputStream = ByteArrayOutputStream()
        logging.debug { "Image converted to grayscale" }

        logging.debug { "Writing image to output stream" }
        withContext(Dispatchers.IO) {
            ImageIO.write(grayscaleImage, "png", outputStream)
        }

        logging.debug { "Image written to output stream" }
        return outputStream.toByteArray()
    }
}
