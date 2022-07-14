package no.nav.openapi


abstract class ContentAssertion(val description: String, val throwfunction: (Int, Int, String) -> Unit) {
    private val missing: MutableList<Any> = mutableListOf()
    private val superfluous: MutableList<Any> = mutableListOf()

    fun evaluate() {
        if (missing.isNotEmpty() || superfluous.isNotEmpty()) {
            throwfunction(
                missing.size,
                superfluous.size,
                "$description\n${missingToString()}\n${superfluousToString()}"
            )
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

internal class PathAssertion :
    ContentAssertion(
        description = "Incorrect paths in apidoc",
        throwfunction = { missing, superflous, message -> throw OpenApiPathError(missing, superflous, message) }
    ) {
    internal class OpenApiPathError(val missing: Int, val superflous: Int, message: String) : AssertionError(message)
}

internal class MethodAssertion :
    ContentAssertion(
        description = "Incorrect methods in apiddoc",
        throwfunction = { missing, superflous, message -> throw OpenApiMethodError(missing, superflous, message) }) {

    data class MethodErrorContent(val path: String, val methods: List<String>) {
        override fun toString(): String = "$path: $methods"
    }

    internal class OpenApiMethodError(missing: Int, superflous: Int, message: String) : AssertionError(message)
}

private fun Int.are(): String = this.let {
    if (it <= 1) {
        "$this is"
    } else {
        "$this are"
    }
}
