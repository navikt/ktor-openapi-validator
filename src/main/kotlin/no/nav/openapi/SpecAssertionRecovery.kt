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