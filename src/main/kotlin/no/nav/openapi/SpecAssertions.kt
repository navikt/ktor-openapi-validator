package no.nav.openapi

internal interface OpenApiContent

internal abstract class ContentAssertion(val description: String) {
    protected val missing: MutableList<OpenApiContent> = mutableListOf()
    protected val superfluous: MutableList<OpenApiContent> = mutableListOf()
    protected abstract fun throwAssertionError()

    fun evaluate() {
        if (missing.isNotEmpty() || superfluous.isNotEmpty()) {
            throwAssertionError()
        }
    }

    fun addMissing(it: List<OpenApiContent>) = missing.addAll(it.map { it })
    fun addSuperfluous(it: List<OpenApiContent>) = superfluous.addAll(it.map { it })
    private fun missingToString() = "${missing.size.are()} missing: $missing"
    private fun superfluousToString() = "${superfluous.size.are()} superfluous:$superfluous"
    protected fun errordescription() = "$description\n${missingToString()}\n${superfluousToString()}"
}

internal class PathAssertion : ContentAssertion(description = "Incorrect paths in apidoc") {
    internal class OpenApiPathError(val missing: Int, val superfluous: Int, message: String) : AssertionError(message)

    override fun throwAssertionError() {
        throw OpenApiPathError(missing.size, superfluous.size, errordescription())
    }
}

internal class MethodAssertion : ContentAssertion(description = "Incorrect methods in apiddoc") {

    override fun throwAssertionError() {
        throw OpenApiMethodError(
            missing.map { it as Method },
            superfluous.map { it as Method },
            errordescription()
        )
    }

    internal class OpenApiMethodError(
        private val missingContent: List<Method>,
        private val superfluousContent: List<Method>,
        message: String
    ) : AssertionError(message) {
        val missing = missingContent.size
        val superfluous = superfluousContent.size

        fun missingInPath(pathString: String): List<String> =
            missingContent.filter { it.parent == pathString }.map { it.operation }

        fun superflousInPath(pathString: String): List<String> =
            superfluousContent.filter { it.parent == pathString }.map { it.operation }
    }
}

private fun Int.are(): String = this.let {
    if (it <= 1) {
        "$this is"
    } else {
        "$this are"
    }
}
