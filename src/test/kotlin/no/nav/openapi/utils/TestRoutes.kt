package no.nav.openapi.utils

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.openapi.Method
import no.nav.openapi.Path
import no.nav.openapi.utils.SimpleTestRoute.simpleTestRoute

private val userDir = System.getProperty("user.dir")
internal class TestPath(pathString: String, methods: List<Method>) :Path(pathString, methods)
internal object SimpleTestRoute {
    val spechFilePathWith1MissingAnd2superflous: String ="$userDir/src/test/resources/openapi/simpleapi-error-in-paths.json"
    internal val testPath = "/test/simple"
    internal val correctSpecFile = "$userDir/src/test/resources/openapi/simpleapi.json"
    internal val expectedPaths = listOf(
        TestPath(pathString = testPath, methods = listOf(
            Method("get"), Method("post")
        )),
        TestPath(pathString = "$testPath/{id}", methods = listOf(
            Method("get")
        ))
    )
    internal fun Application.simpleTestRoute() {
        routing {
            route(testPath) {
                get { }
                post { }
            }
            route("$testPath/{id}") {
                get { }
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