package io.valentinsoare.noir.service

import java.net.URL

interface InputParserService {
    fun validateImageUrl(urlString: String): URL
    fun isValidImageUrl(urlString: String): Boolean
    fun generateOutputFilename(url: String, customName: String?): String
}
