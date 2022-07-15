package no.nav.openapi.utils

import io.ktor.server.application.Application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.openapi.Method
import no.nav.openapi.Path
import no.nav.openapi.utils.SimpleTestRoute.simpleTestRoute

internal val userDir = System.getProperty("user.dir")

internal class TestPath(pathString: String, methods: List<Method>) : Path(pathString, methods)
internal object SimpleTestRoute {
    val `specfile with 2 superflous and 1 missing path ` =
        "$userDir/src/test/resources/openapi/simpleapi-error-in-paths.json"
    val `specfile with 3 missing and 2 superflous methods` =
        "$userDir/src/test/resources/openapi/simpleapi-error-in-methods.json"
    internal val testPath = "/test/simple"
    internal val correctSpecFile = "$userDir/src/test/resources/openapi/simpleapi.json"
    internal val expectedPaths = listOf(
        TestPath(
            pathString = testPath, methods = listOf(
                Method("get"), Method("post")
            )
        ),
        TestPath(
            pathString = "$testPath/{id}", methods = listOf(
                Method("get")
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

internal fun withSimpleRoute(block: Application.() -> Unit) {
    TestApplication.withMockAuthServerAndTestApplication({
        simpleTestRoute()
    }) {
        this.application {
            block()
        }
    }
}