package io.valentinsoare.noir

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.system.exitProcess

@SpringBootApplication
class NoirApplication

fun main(args: Array<String>) {
    val context = SpringApplication.run(NoirApplication::class.java, *args)
    exitProcess(SpringApplication.exit(context))
}
