package no.nav.openapi

import java.io.File

internal class SpecAssertionRecovery(
    private val openApiSpec: OpenApiSpec,
    private val application: ApplicationSpec,
    val recoveryFilePath: String = "build/tmp/openapi.json"
) {

    internal var pathAssertionError = false
    internal var methodAssertionError = false

    fun writeToFile() {
        if (pathAssertionError || methodAssertionError) {
            openApiSpec.updateSpec(application)
            File(recoveryFilePath).writeBytes(openApiSpec.toJson())
            throw MissingSpecContentError(recoveryFilePath)
        }
    }
}

internal class MissingSpecContentError(filelocation: String) :
    AssertionError("Updated spec was written to $filelocation, but requires additional information")

internal fun withRecovery(recovery: SpecAssertionRecovery, vararg assertions: () -> Unit) {
    assertions.forEach {
        try {
            it()
        } catch (pathError: PathAssertion.OpenApiPathError) {
            recovery.pathAssertionError = true
        } catch (methodError: MethodAssertion.OpenApiMethodError) {
            recovery.methodAssertionError = true
        } finally {
            recovery.writeToFile()
        }
    }
}
