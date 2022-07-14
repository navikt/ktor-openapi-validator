package no.nav.openapi

import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing

internal class ApplicationSpec(private val paths: List<Path.ApplicationPath>) {
    companion object {
        internal fun Routing.routesInApplication(): ApplicationSpec {
            val applicationPaths = allRoutes(this)
                .filter { it.selector is HttpMethodRouteSelector }
                .groupBy { it.parent }
                .map { routeMap ->
                        Path.ApplicationPath(
                            pathString = routeMap.key.toString(),
                            methods = methodsInPath(routeMap)
                    )
                }
            return ApplicationSpec(applicationPaths)
        }

        private fun methodsInPath(route: Map.Entry<Route?, List<Route>>): MutableList<Method> {
            val methods = mutableListOf<Method>()
            route.value.map { methods.add(Method((it.selector as HttpMethodRouteSelector).method.value.lowercase())) }
            return methods
        }
    }

    internal fun missingPaths(other: List<Path>) = this.paths.filterNot { other.contains(it) }
    internal fun superfluousPaths(other: List<Path>): List<Path> = other.filterNot { this.paths.contains(it) }
    internal fun missingMethods(other: List<Path>): List<Map<String, List<Method>>> {
        val methodsThatShouldBePresent = this.paths.map { mapOf(it.pathString to it.methods) }
        val methodsPresentInOther = other.map { mapOf(it.pathString to it.methods) }

        TODO("Not yet implemented")
    }

    fun superfluousMethods(): Any {
        TODO("Not yet implemented")
    }

    fun pathCount() = this.paths.size
}

internal class OpenApiSpec(var paths: List<Path>, private val serDer: OpenApiSerDer) {
    companion object {
        fun fromJson(openapiFilePath: String): OpenApiSpec {
            return OpenApiSerDer.fromFile(openapiFilePath).let {
                OpenApiSpec(
                    paths = it.paths.map { path ->
                        Path.OpenApiSpecPath(
                            pathString = path.key,
                            methods = path.value.map { method -> Method(method.key) }
                        )
                    },
                    serDer = it
                )
            }
        }
    }

    infix fun `should contain the same paths as`(application: ApplicationSpec) {
        val pathAssertion = PathAssertion()
        application.missingPaths(this.paths).let {
            pathAssertion.addMissing(it)
        }
        application.superfluousPaths(this.paths).let {
            pathAssertion.addsuperfluous(it)
        }
        pathAssertion.evaluate()
    }

    infix fun `paths should have the same methods as`(application: ApplicationSpec) {
        val methodAssertionError = MethodAssertion()
        application.missingMethods(this.paths).let {}
        application.superfluousMethods().let {}
    }

    internal fun updatePaths(application: ApplicationSpec) {
        val pathsToBeAdded = application.missingPaths(this.paths)
        val pathsToBeRemoved = application.superfluousPaths(this.paths)
        this.paths = this.paths - pathsToBeRemoved + pathsToBeAdded
    }

    internal fun toJson(): ByteArray = serDer.generateNewSpecFile(this).toByteArray()
}

abstract class Path(val pathString: String, val methods: List<Method>) {
    override fun equals(other: Any?): Boolean {
        require(other is Path)
        return this.pathString.compareTo(other.pathString) == 0
    }

    override fun hashCode(): Int {
        var result = pathString.hashCode()
        result = 31 * result + methods.hashCode()
        return result
    }

    internal class ApplicationPath(pathString: String, methods: List<Method>) : Path(pathString, methods) {
        fun missingMethods() {
            TODO("Not yet implemented")
        }

        fun superflousMethods(other: List<Path>) {
        }
    }

    internal class OpenApiSpecPath(pathString: String, methods: List<Method>) : Path(pathString, methods)
}

data class Method(val operation: String)

private fun allRoutes(root: Route): List<Route> = listOf(root) + root.children.flatMap { allRoutes(it) }
