package no.nav.openapi.utils

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.openapi.Method
import no.nav.openapi.Method.ApplicationMethod
import no.nav.openapi.Path
import no.nav.openapi.utils.SimpleTestRoute.simpleTestRoute

internal val userDir = System.getProperty("user.dir")

internal class TestPath(pathString: String, methods: List<Method>) : Path(pathString, methods)
internal object SimpleTestRoute {
    val `specfile with 2 superflous and 1 missing path ` =
        "$userDir/src/test/resources/openapi/simpleapi-error-in-paths.json"
    val `specfile with 3 missing and 2 superflous methods` =
        "$userDir/src/test/resources/openapi/simpleapi-error-in-methods.json"
    val `specfile with 1 missing and 2 superflous paths, 2 superflous and 2 missing methods` =
        "$userDir/src/test/resources/openapi/simpleapi-error-in-methods-and-paths.json"

    internal val testPath = "/test/simple"
    internal val correctSpecFile = "$userDir/src/test/resources/openapi/simpleapi.json"
    internal val expectedPaths = listOf(
        TestPath(
            pathString = testPath,
            methods = listOf(
                ApplicationMethod(testPath, "get"), ApplicationMethod(testPath, "post")
            )
        ),
        TestPath(
            pathString = "$testPath/{id}",
            methods = listOf(
                ApplicationMethod("$testPath/{id}", "get")
            )
        )
    )

    internal fun Application.simpleTestRoute() {
        routing {
            route(testPath) {
                get { }
                post { }
            }
            route("$testPath/{id}") {
                get { }
                post { }
                delete { }
                put { }
            }
        }
    }
}

internal object AutenticatesTestRoute {
    val correctSpecFilePath = "$userDir/src/test/resources/openapi/autehnticatedapi.json"
    internal fun Application.authRoutes() {

        routing {
            route("simple/azuread") {
                authenticate(Config.AzureAd.name) {
                    route("/test1") {
                        get { }
                        post { }
                    }
                    route("test1/{id}") {
                        get {}
                    }
                    route("test1/summary") {
                        get {}
                        post { }
                        put { }
                        delete { }
                    }
                }
            }
            route("simple/tokenx") {
                authenticate(Config.TokenX.name) {
                }
            }
        }
    }
}

internal fun withSimpleRoute(block: Application.() -> Unit) {
    TestApplication.withMockAuthServerAndTestApplication({
        simpleTestRoute()
    }) {
        this.application {
            block()
        }
    }
}
