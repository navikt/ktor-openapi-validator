package no.nav.openapi

import java.io.File

internal class SpecAssertionRecovery(
    val openApiSpec: OpenApiSpec,
    val application: ApplicationSpec,
    private val recoveryFilePath: String = "build/tmp/openapi.json"
) {
    private var pathAssertionRecovered = false
    private var methodAssertionRecovered = false
    fun pathAssertionError() {
        openApiSpec.updatePaths(application)
        pathAssertionRecovered = true
    }

    fun metohdAssertionError() {
        methodAssertionRecovered = true
    }

    fun writeToFile() {
        if (pathAssertionRecovered && methodAssertionRecovered) {
            File(recoveryFilePath).writeBytes(openApiSpec.toJson())
            throw MissingSpecContentError(recoveryFilePath)
        }
    }
}

abstract class ContentAssertionError {
    private val missing: MutableList<Any> = mutableListOf()
    private val superfluous: MutableList<Any> = mutableListOf()

    abstract fun message(): String
    fun evaluate() {
        if (missing.isNotEmpty() || superfluous.isNotEmpty()) {
            throw AssertionError("${message()}\n${missingToString()}\n${superfluousToString()} ")
        }
    }

    fun addMissing(it: List<Path>) {
        missing.addAll(it.map { it.value })
    }

    fun addsuperfluous(it: List<Path>) {
        superfluous.addAll(it.map { it.value })
    }

    private fun missingToString(): String = "${missing.size.are()} missing: $missing"
    private fun superfluousToString(): String = "${superfluous.size.are()} superfluous:$superfluous"
}

internal class PathAssertionError : ContentAssertionError() {
    override fun message(): String = "Incorrect paths in apidoc"
}

private fun Int.are(): String = this.let {
    if (it <= 1) {
        "$this is"
    } else {
        "$this are"
    }
}

internal class MethodAssertionError() : ContentAssertionError() {
    data class MethodErrorContent(val path: String, val methods: List<String>) {
        override fun toString(): String = "$path: $methods"
    }
    override fun message(): String = "Incorrect methods in apiddoc"
}
internal class MissingSpecContentError(filelocation: String) :
    AssertionError("Updated spec was written to $filelocation, but requires additional information")

internal fun withRecovery(recovery: SpecAssertionRecovery, function: () -> Unit) {
    try {
        function()
    } catch (pathAssertionError: AssertionError) {
        recovery.pathAssertionError()
    } finally {
        // TODO
        recovery.metohdAssertionError()
        recovery.writeToFile()
    }
}
