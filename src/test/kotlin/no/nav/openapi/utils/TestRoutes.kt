package no.nav.openapi.utils

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

private val userDir = System.getProperty("user.dir")
internal object SimpleTestRoute {
    internal val spechFilePath = "$userDir/src/test/resources/simpleapi.json"
    internal fun Application.simpleTestRoute() {
        routing {
            route("/test/simple/") {
                get { }
                post { }
            }
        }
    }
}
