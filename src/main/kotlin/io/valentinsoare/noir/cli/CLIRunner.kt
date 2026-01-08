package io.valentinsoare.noir.cli

import io.valentinsoare.noir.annotationandinterface.WithLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.stereotype.Component
import picocli.CommandLine
import picocli.CommandLine.IFactory

@Component
class CLIRunner(
    private val imageProcessorCommand: ImageProcessorCommand,
    private val factory: IFactory
) : CommandLineRunner, ExitCodeGenerator, WithLogging {
    private var exitCode: Int = 0

    override fun run(vararg args: String) {
        logging.debug { "CLI Runner started with args: ${args.joinToString()}" }

        exitCode = CommandLine(imageProcessorCommand, factory)
            .setExecutionExceptionHandler { ex, _, _ ->
                logging.error(ex) { "Execution exception" }
                System.err.println("Error: ${ex.message}")
                ImageProcessorCommand.ExitCode.GENERAL_ERROR
            }
            .setParameterExceptionHandler { ex, _ ->
                System.err.println("Error: ${ex.message}")
                System.err.println()
                ex.commandLine.usage(System.err)
                ImageProcessorCommand.ExitCode.INVALID_INPUT
            }
            .execute(*args)
    }

    override fun getExitCode(): Int = exitCode
}
