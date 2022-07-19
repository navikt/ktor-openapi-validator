package no.nav.openapi

import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import no.nav.openapi.Method.ApplicationMethod
import no.nav.openapi.Method.OpenApiMethod
import no.nav.openapi.Path.ApplicationPath

internal class ApplicationRoutes(private val paths: List<ApplicationPath>) {
    private val methods: List<ApplicationMethod> = this.paths.flatMap { it.methods } as List<ApplicationMethod>
    val pathcount = this.paths.size

    companion object {
        internal fun Routing.routesInApplication(): ApplicationRoutes {
            val applicationPaths = allRoutes(this)
                .filter { it.selector is HttpMethodRouteSelector }
                .groupBy { it.parent }
                .map { routeMap ->
                    ApplicationPath(
                        pathString = routeMap.key.toString(),
                        methods = methodsInPath(routeMap)
                    )
                }
            return ApplicationRoutes(applicationPaths)
        }

        private fun methodsInPath(route: Map.Entry<Route?, List<Route>>): MutableList<ApplicationMethod> {
            val methods = mutableListOf<ApplicationMethod>()
            val parent = route.key.toString()
            route.value.map {
                methods.add(
                    ApplicationMethod(
                        operation = (it.selector as HttpMethodRouteSelector).method.value.lowercase(),
                        parent = parent
                    )
                )
            }
            return methods
        }
    }

    internal fun missingPaths(other: List<Path>) = this.paths.filterNot { other.contains(it) }
    internal fun superfluousPaths(other: List<Path>) = other.filterNot { this.paths.contains(it) }
    internal fun missingMethods(other: List<Method>) = this.methods.filterNot { other.contains(it) }
    internal fun superfluousMethods(other: List<Method>) = other.filterNot { this.methods.contains(it) }
    fun openApiPaths(): List<Path.OpenApiSpecPath> {
        return this.paths.map {
            Path.OpenApiSpecPath(
                pathString = it.pathString,
                methods = it.methods.map { OpenApiMethod(it.parent, it.operation) }
            )
        }
    }
}

internal class OpenApiSpec(var paths: List<Path>, private val serDer: OpenApiSerDer) {
    private val methods = paths.flatMap { it.methods }

    companion object {
        fun fromJson(openapiFilePath: String): OpenApiSpec {
            return OpenApiSerDer.fromFile(openapiFilePath).let {
                OpenApiSpec(
                    paths = it.paths.map { path ->
                        Path.OpenApiSpecPath(
                            pathString = path.key,
                            methods = path.value.map { method -> OpenApiMethod(path.key, method.key) }
                        )
                    },
                    serDer = it
                )
            }
        }
    }

    infix fun `should contain the same paths as`(application: ApplicationRoutes) {
        val pathAssertion = PathAssertion()
        application.missingPaths(this.paths).let {
            pathAssertion.addMissing(it)
        }
        application.superfluousPaths(this.paths).let {
            pathAssertion.addSuperfluous(it)
        }
        pathAssertion.evaluate()
    }

    infix fun `paths should have the same methods as`(application: ApplicationRoutes) {
        val methodAssertionError = MethodAssertion()
        application.missingMethods(this.methods).let {
            methodAssertionError.addMissing(it)
        }
        application.superfluousMethods(this.methods).let {
            methodAssertionError.addSuperfluous(it)
        }

        methodAssertionError.evaluate()
    }

    internal fun updateSpec(application: ApplicationRoutes) {
        this.paths = application.openApiPaths()
    }

    internal fun toJson(): ByteArray = serDer.generateNewSpecFile(this).toByteArray()
}

internal abstract class Path(val pathString: String, val methods: List<Method>) : OpenApiContent {
    override fun equals(other: Any?): Boolean {
        require(other is Path)
        return this.pathString.compareTo(other.pathString) == 0
    }

    override fun hashCode(): Int {
        var result = pathString.hashCode()
        result = 31 * result + methods.hashCode()
        return result
    }

    internal class ApplicationPath(pathString: String, methods: List<ApplicationMethod>) : Path(pathString, methods)

    internal class OpenApiSpecPath(pathString: String, methods: List<OpenApiMethod>) : Path(pathString, methods)
}

internal abstract class Method(val parent: String, val operation: String) : OpenApiContent {
    internal class OpenApiMethod(parent: String, operation: String) : Method(parent, operation)
    internal class ApplicationMethod(parent: String, operation: String) : Method(parent, operation)

    override fun equals(other: Any?): Boolean {
        require(other is Method)
        return this.operation == other.operation && this.parent == other.parent
    }
}

private fun allRoutes(root: Route): List<Route> = listOf(root) + root.children.flatMap { allRoutes(it) }
