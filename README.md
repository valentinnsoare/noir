<h1 align="center">Noir</h1>

<p align="center">
  <b>Converting your world to grayscale, one image at a time.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.2.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.0-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot"/>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#demo">Demo</a>
</p>

---

A modern CLI tool for fetching images from URLs and converting them to grayscale (black and white).

Built with **Kotlin**, **Spring Boot 4**, and **Picocli**, demonstrating clean architecture principles and modern development practices.

<br>

> [!NOTE]
> Project is :100: completed!

<br>


## Features

| Feature | Description |
|---------|-------------|
| **Multi-Format Support** | Process JPEG, PNG, GIF, BMP, and WebP images seamlessly |
| **Async Operations** | Non-blocking I/O using Kotlin Coroutines for optimal performance |
| **Progress Tracking** | Real-time progress indicators for long-running operations |
| **Flexible Output** | Configurable output directory and custom filenames |
| **Developer Friendly** | Verbose and quiet modes for different use cases |
| **Clean Architecture** | SOLID principles and design patterns for maintainability |

### Highlights

```
+------------------------+     +------------------------+     +------------------------+
|     URL Fetching       |     |   Grayscale Convert    |     |     PNG Output         |
|------------------------|     |------------------------|     |------------------------|
| - HTTP/HTTPS support   |     | - High-quality algo    |     | - Lossless format      |
| - Async WebClient      | --> | - Luminosity method    | --> | - Configurable path    |
| - 3MB buffer limit     |     | - AWT ImageIO          |     | - Custom naming        |
+------------------------+     +------------------------+     +------------------------+
```

## Quick Start

```bash
# Build the application
./mvnw clean package

# Run with a URL
java -jar target/noir-1.0.0.jar https://example.com/image.jpg

# Custom output filename
java -jar target/noir-1.0.0.jar https://example.com/image.jpg -o my_image.png

# Custom output directory
java -jar target/noir-1.0.0.jar https://example.com/image.jpg -d ./processed
```

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [SOLID Principles](#solid-principles)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Technology Stack](#technology-stack)
- [Exit Codes](#exit-codes)
- [Demo](#demo)
- [Acknowledgments](#acknowledgments)
- [License](#license)

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+** (or use the included Maven Wrapper)

## Installation

### Using Maven Wrapper (Recommended)

```bash
# Clone the repository
git clone https://github.com/valentinsoare/noir.git
cd noir

# Build the project
./mvnw clean package

# The executable JAR will be at target/noir-1.0.0.jar
```

### Using Installed Maven

```bash
mvn clean package
```

## Usage

### Basic Usage

```bash
# Convert an image to grayscale
java -jar target/noir-1.0.0.jar <image-url>

# Example
java -jar target/noir-1.0.0.jar https://picsum.photos/800/600
```

### Command Options

| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--output` | `-o` | Custom output filename | `processed_image_<timestamp>.png` |
| `--output-dir` | `-d` | Output directory | Current directory (`.`) |
| `--quiet` | `-q` | Suppress progress output | `false` |
| `--verbose` | `-v` | Enable verbose logging | `false` |
| `--help` | `-h` | Show help message | - |
| `--version` | `-V` | Show version info | - |

### Examples

```bash
# Save with custom filename
java -jar target/noir-1.0.0.jar https://example.com/photo.jpg -o vacation.png

# Save to specific directory
java -jar target/noir-1.0.0.jar https://example.com/photo.jpg -d ./grayscale-images

# Quiet mode (no progress output)
java -jar target/noir-1.0.0.jar https://example.com/photo.jpg -q

# Verbose mode (debug logging)
java -jar target/noir-1.0.0.jar https://example.com/photo.jpg -v

# Combine options
java -jar target/noir-1.0.0.jar https://example.com/photo.jpg -o result.png -d ./output -v
```

## Architecture

Noir follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    CLI Layer                            │
│    ImageProcessorCommand (Picocli Command)              │
│    CLIRunner (Spring CommandLineRunner)                 │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│               Service Layer (Abstractions)              │
│    ImageFetcherService    ImageProcessingService        │
│    InputParserService     CatchThatExceptionHandler     │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│            Service Implementation Layer                 │
│    ImageFetcherServiceImpl    ImageProcessingServiceImpl│
│    InputParserServiceImpl     CatchThatExceptionHandler │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                Infrastructure Layer                     │
│    Spring WebClient    Java AWT/ImageIO                 │
│    Kotlin Coroutines   Java NIO                         │
└─────────────────────────────────────────────────────────┘
```

### Data Flow

```
User Input (CLI args)
        │
        ▼
┌───────────────────┐
│  CLIRunner        │ ──→ Parses args, invokes command
└───────────────────┘
        │
        ▼
┌───────────────────┐
│ ImageProcessor    │ ──→ Validates input, orchestrates pipeline
│ Command           │
└───────────────────┘
        │
        ├──→ InputParserService (URL validation)
        │
        ├──→ ImageFetcherService (HTTP GET → ByteArray)
        │
        ├──→ ImageProcessingService (Grayscale conversion)
        │
        └──→ Files.write() (Save to disk)
```

### Concurrency Model

The application uses **Kotlin Coroutines** with specialized dispatcher scopes:

- **IO Scope** (`Dispatchers.IO`): Network operations, file I/O
- **Processing Scope** (`Dispatchers.Default`): CPU-intensive image processing

```kotlin
@IOScope private val ioScope: CoroutineScope
@ProcessingScope private val processingScope: CoroutineScope
```

## Design Patterns

### 1. Command Pattern

The `ImageProcessorCommand` encapsulates the image processing request as an object:

```kotlin
@Command(name = "noir")
class ImageProcessorCommand() : Callable<Int> {
    override fun call(): Int {
        
    }
}
```

### 2. Strategy Pattern

Service interfaces allow swapping implementations without changing client code:

```kotlin
interface ImageProcessingService {
    suspend fun processImage(imageBytes: ByteArray): ByteArray
}

@Service
class ImageProcessingServiceImpl : ImageProcessingService {  }
```

### 3. Facade Pattern

`ImageProcessorCommand.call()` provides a simplified interface to the complex image pipeline:

```kotlin
override fun call(): Int {
    /* Validates input
     Fetches image
     Processes image
     Saves result
     Returns exit code */
}
```

### 4. Dependency Injection

Spring manages all component dependencies via constructor injection:

```kotlin
class ImageProcessorCommand(
    private val imageFetcherService: ImageFetcherService,
    private val imageProcessingService: ImageProcessingService,
    private val inputParserService: InputParserService,
    @IOScope private val ioScope: CoroutineScope,
    @ProcessingScope private val processingScope: CoroutineScope
) : Callable<Int>
```

### 5. Decorator Pattern (Mixin)

The `WithLogging` interface adds logging capability without inheritance pollution:

```kotlin
interface WithLogging {
    val logging: KLogger
        get() = KotlinLogging.logger(this::class.java.name)
}

class ImageProcessingServiceImpl : ImageProcessingService, WithLogging {
    fun doSomething() {
        logging.info { "Processing..." }
    }
}
```

### 6. Template Method Pattern

The `executeWithProgress()` method defines a skeleton for task execution:

```kotlin
private suspend fun <T> executeWithProgress(
    scope: CoroutineScope,
    taskName: String,
    action: suspend () -> T
): T? {
    // launch → show progress → await → handle errors
}
```

## SOLID Principles

### Single Responsibility Principle (SRP)

Each class has one clear responsibility:

| Class | Responsibility |
|-------|----------------|
| `ImageFetcherServiceImpl` | HTTP image retrieval only |
| `ImageProcessingServiceImpl` | Grayscale conversion only |
| `InputParserServiceImpl` | URL validation and filename generation only |
| `CLIRunner` | CLI framework integration only |
| `ImageProcessorCommand` | Pipeline orchestration and user interaction only |

### Open/Closed Principle (OCP)

- Service interfaces allow extension via new implementations
- Adding new image processors doesn't require modifying existing code
- Configuration classes allow adding new beans without changing command class

### Liskov Substitution Principle (LSP)

All service implementations correctly fulfill their interface contracts:

```kotlin
val fetcher: ImageFetcherService = ImageFetcherServiceImpl()
// or
val fetcher: ImageFetcherService = CustomImageFetcherServiceImpl()
```

### Interface Segregation Principle (ISP)

Small, focused interfaces:

```kotlin
interface ImageFetcherService {
    suspend fun getImage(url: String): ByteArray
}

interface ImageProcessingService {
    suspend fun processImage(imageBytes: ByteArray): ByteArray
}

interface InputParserService {
    fun validateImageUrl(urlString: String): URL
    fun isValidImageUrl(urlString: String): Boolean
    fun generateOutputFilename(url: String, customName: String?): String
}
```

### Dependency Inversion Principle (DIP)

High-level modules depend on abstractions:

```kotlin
class ImageProcessorCommand(
    private val imageFetcherService: ImageFetcherService,  
    private val imageProcessingService: ImageProcessingService,
    private val inputParserService: InputParserService 
)
```

## Project Structure

```
noir/
├── pom.xml                                    # Maven configuration
├── mvnw, mvnw.cmd                             # Maven Wrapper scripts
├── src/
│   ├── main/
│   │   ├── kotlin/io/valentinsoare/noir/
│   │   │   ├── NoirApplication.kt             # Application entry point
│   │   │   ├── utils/
│   │   │   │   └── AdditionalComponents.kt    # Custom annotations & interfaces
│   │   │   ├── cli/
│   │   │   │   ├── CLIRunner.kt               # Spring CommandLineRunner
│   │   │   │   └── ImageProcessorCommand.kt   # Picocli command definition
│   │   │   ├── configuration/
│   │   │   │   ├── CoroutineConfiguration.kt  # Coroutine scope beans
│   │   │   │   └── WebClientConfiguration.kt  # WebClient bean
│   │   │   ├── handler/
│   │   │   │   ├── CatchThatExceptionHandler.kt
│   │   │   │   └── CatchThatExceptionHandlerImpl.kt
│   │   │   └── service/
│   │   │       ├── ImageFetcherService.kt     # Interface
│   │   │       ├── ImageFetcherServiceImpl.kt # Implementation
│   │   │       ├── ImageProcessingService.kt  # Interface
│   │   │       ├── ImageProcessingServiceImpl.kt
│   │   │       ├── InputParserService.kt      # Interface
│   │   │       └── InputParserServiceImpl.kt
│   │   └── resources/
│   │       ├── application.yaml               # Spring Boot configuration
│   │       └── logback.xml                    # Logging configuration
│   └── test/
│       └── kotlin/io/valentinsoare/noir/
│           └── NoirApplicationTests.kt        # Application context tests
└── target/                                    # Build output
```

### Package Responsibilities

| Package         | Purpose |
|-----------------|---------|
| `cli`           | Command-line interface components |
| `service`       | Business logic interfaces and implementations |
| `configuration` | Spring bean configurations |
| `handler`       | Exception handling |
| `utils`         | Custom annotations and shared interfaces |

## Configuration

### Application Configuration (`application.yaml`)

```yaml
spring:
  main:
    banner-mode: off      # Disable Spring Boot banner
  application:
    name: "Noir"

logging:
  level:
    root: off             # Logging controlled via logback.xml
```

### Logging Configuration (`logback.xml`)

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### WebClient Configuration

The HTTP client is configured with a 3MB buffer limit:

```kotlin
@Configuration
class WebClientConfiguration {
    @Bean
    fun webClient(): WebClient = WebClient.builder()
        .codecs { it.defaultCodecs().maxInMemorySize(3 * 1024 * 1024) }
        .build()
}
```

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.2.21 | Programming language |
| **Java** | 21 | Runtime |
| **Spring Boot** | 4.0.0 | Application framework |
| **Picocli** | 4.7.7 | CLI framework |
| **Kotlinx Coroutines** | 1.10.2 | Async programming |
| **Spring WebClient** | - | Reactive HTTP client |
| **Kotlin Logging** | 7.0.3 | Logging facade |
| **Logback** | - | Logging implementation |
| **Maven** | 3.8+ | Build tool |

## Exit Codes

| Code | Name | Description |
|------|------|-------------|
| `0` | SUCCESS | Image processed successfully |
| `1` | GENERAL_ERROR | Unexpected error occurred |
| `2` | INVALID_INPUT | Invalid URL or arguments |
| `3` | NETWORK_ERROR | Failed to fetch image |
| `4` | PROCESSING_ERROR | Failed to convert image |
| `5` | IO_ERROR | Failed to save output file |
| `6` | USER_CANCELLED | Operation cancelled by user |

## Demo

https://github.com/user-attachments/assets/6d8a1fd0-be60-4891-9c18-ea4595f550f5

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Picocli](https://picocli.info/) - CLI framework
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async programming
- [shields.io](https://shields.io/) - README badges

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with Kotlin
</p>