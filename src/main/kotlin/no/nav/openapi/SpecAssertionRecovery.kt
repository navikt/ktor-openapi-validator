package no.nav.openapi

import java.io.File

internal class SpecAssertionRecovery(
    private val openApiSpec: OpenApiSpec,
    private val application: ApplicationRoutes,
    val recoveryFilePath: String = "build/tmp/openapi.json"
) {

    internal var pathAssertionError = false
    internal var methodAssertionError = false

    fun assertSpecAndRecover() {
        withRecovery(this) { openApiSpec `should contain the same paths as` application }
        withRecovery(this) { openApiSpec `paths should have the same methods as` application }
        writeToFile()
    }

    private fun writeToFile() {
        if (pathAssertionError || methodAssertionError) {
            openApiSpec.updateSpec(application)
            File(recoveryFilePath).writeBytes(openApiSpec.toJson())
            throw MissingSpecContentError(recoveryFilePath)
        }
    }
}

internal class MissingSpecContentError(filelocation: String) :
    AssertionError("Updated spec was written to $filelocation, but requires additional information")

private fun withRecovery(recovery: SpecAssertionRecovery, assertion: () -> Unit) {
    try {
        assertion()
    } catch (pathError: PathAssertion.OpenApiPathError) {
        recovery.pathAssertionError = true
    } catch (methodError: MethodAssertion.OpenApiMethodError) {
        recovery.methodAssertionError = true
    }
}
