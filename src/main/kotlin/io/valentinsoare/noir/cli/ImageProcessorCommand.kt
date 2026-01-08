package io.valentinsoare.noir.cli

import io.valentinsoare.noir.utils.IOScope
import io.valentinsoare.noir.utils.ProcessingScope
import io.valentinsoare.noir.utils.WithLogging
import io.valentinsoare.noir.service.ImageFetcherService
import io.valentinsoare.noir.service.ImageProcessingService
import io.valentinsoare.noir.service.InputParserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable

@Component
@Command(
    name = "noir",
    mixinStandardHelpOptions = true,
    version = ["Noir 0.0.1"],
    description = [
        "Noir - Fetches an image from a URL and converts it to black and white (grayscale).",
        "",
        "Example usage:",
        "  noir https://example.com/image.jpg",
        "  noir https://example.com/image.jpg -o my_image.png",
        "  noir https://example.com/image.jpg --output-dir ./images"
    ],
    footer = [
        "",
        "Supported image formats: JPEG, PNG, GIF, BMP, WebP",
        "Output format is always PNG.",
        "",
        "Exit codes:",
        "  0  Success",
        "  1  General error",
        "  2  Invalid input (bad URL or arguments)",
        "  3  Network error (failed to fetch image)",
        "  4  Processing error (failed to convert image)",
        "  5  I/O error (failed to save output)",
        "  6  User cancelled"
    ]
)
class ImageProcessorCommand(
    private val imageFetcherService: ImageFetcherService,
    private val imageProcessingService: ImageProcessingService,
    private val inputParserService: InputParserService,
    @IOScope private val ioScope: CoroutineScope,
    @ProcessingScope private val processingScope: CoroutineScope
) : Callable<Int>, WithLogging {

    @Parameters(
        index = "0",
        description = ["URL of the image to process"],
        paramLabel = "<image-url>"
    )
    private lateinit var imageUrl: String

    @Option(
        names = ["-o", "--output"],
        description = ["Output filename (default: processed_image_<timestamp>.png)"],
        paramLabel = "<filename>"
    )
    private var outputFilename: String? = null

    @Option(
        names = ["-d", "--output-dir"],
        description = ["Output directory (default: current directory)"],
        paramLabel = "<directory>"
    )
    private var outputDirectory: String = "."

    @Option(
        names = ["-q", "--quiet"],
        description = ["Suppress progress output"]
    )
    private var quiet: Boolean = false

    @Option(
        names = ["-v", "--verbose"],
        description = ["Enable verbose logging"]
    )
    private var verbose: Boolean = false

    override fun call(): Int {
        if (verbose) {
            configureVerboseLogging()
        }

        return try {
            val validatedUrl = try {
                inputParserService.validateImageUrl(imageUrl)
            } catch (e: IllegalArgumentException) {
                printError("Invalid URL: ${e.message}")
                return ExitCode.INVALID_INPUT
            }

            val outputDir = File(outputDirectory)
            if (!outputDir.exists()) {
                val createDir = promptForDirectoryCreation(outputDirectory)
                if (!createDir) {
                    println("Operation cancelled by user.")
                    return ExitCode.USER_CANCELLED
                }
                if (!outputDir.mkdirs()) {
                    printError("Cannot create output directory: $outputDirectory")
                    return ExitCode.IO_ERROR
                }
                println("Directory created: ${outputDir.absolutePath}")
            } else if (!outputDir.isDirectory) {
                printError("Output path is not a directory: $outputDirectory")
                return ExitCode.INVALID_INPUT
            }

            if (verbose) {
                logging.info { "Starting image processing for: $validatedUrl" }
            }

            runBlocking {
                executeImagePipeline(validatedUrl)
            }
        } catch (e: Exception) {
            printError("Unexpected error: ${e.message}")
            logging.error(e) { "Command execution failed" }
            ExitCode.GENERAL_ERROR
        }
    }

    private suspend fun executeImagePipeline(url: java.net.URL): Int {
        val imageBytes = fetchImageWithProgress(url)
        if (imageBytes == null) {
            printError("Failed to fetch image from URL")
            return ExitCode.NETWORK_ERROR
        }

        if (!quiet) {
            println("Image fetched successfully, size: ${imageBytes.size / 1024} KB")
        }

        val processedImage = processImageWithProgress(imageBytes)
        if (processedImage == null) {
            printError("Failed to process image")
            return ExitCode.PROCESSING_ERROR
        }

        if (!quiet) {
            println("Image converted to grayscale, size: ${processedImage.size / 1024} KB")
        }

        val filename = inputParserService.generateOutputFilename(url.toString(), outputFilename)
        val outputPath = Paths.get(outputDirectory, filename)

        return try {
            withContext(Dispatchers.IO) {
                Files.write(outputPath, processedImage)
            }
            if (!quiet) {
                println("Processed image saved to: ${outputPath.toAbsolutePath()}")
                println("Compression: ${imageBytes.size / 1024} KB -> ${processedImage.size / 1024} KB")
            }
            ExitCode.SUCCESS
        } catch (e: Exception) {
            printError("Failed to save image: ${e.message}")
            ExitCode.IO_ERROR
        }
    }

    private suspend fun <T> executeWithProgress(
        scope: CoroutineScope,
        taskName: String,
        action: suspend () -> T
    ): T? = withContext(Dispatchers.Default) {
        val job = scope.async { action() }

        val progressJob = if (!quiet) {
            launch {
                print(taskName)
                while (!job.isCompleted) {
                    print(".")
                    delay(100L)
                }
            }
        } else null

        try {
            val result = job.await()
            progressJob?.cancel()

            if (!quiet) println(" DONE")
            result
        } catch (e: Exception) {
            progressJob?.cancel()

            if (!quiet) println(" FAILED")
            logging.error(e) { "Failed to execute $taskName" }
            null
        }
    }

    private suspend fun fetchImageWithProgress(url: java.net.URL): ByteArray? =
        executeWithProgress(
            scope = ioScope,
            taskName = "Fetching image"
        ) {
            imageFetcherService.getImage(url.toString())
        }

    private suspend fun processImageWithProgress(imageBytes: ByteArray): ByteArray? =
        executeWithProgress(
            scope = processingScope,
            taskName = "Processing image"
        ) {
            imageProcessingService.processImage(imageBytes)
        }

    private fun printError(message: String) {
        System.err.println("Error: $message")
    }

    private fun promptForDirectoryCreation(directory: String): Boolean {
        print("Directory '$directory' does not exist. Would you like to create it? (yes/no): ")
        System.out.flush()

        val reader = BufferedReader(InputStreamReader(System.`in`))
        val response = reader.readLine()?.trim()?.lowercase() ?: ""

        return response == "yes" || response == "y"
    }

    object ExitCode {
        const val SUCCESS = 0
        const val GENERAL_ERROR = 1
        const val INVALID_INPUT = 2
        const val NETWORK_ERROR = 3
        const val PROCESSING_ERROR = 4
        const val IO_ERROR = 5
        const val USER_CANCELLED = 6
    }
}
