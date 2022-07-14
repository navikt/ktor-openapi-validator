package no.nav.openapi

import io.ktor.server.application.plugin
import io.ktor.server.routing.Routing
import no.nav.openapi.utils.SimpleTestRoute
import no.nav.openapi.utils.SimpleTestRoute.simpleTestRoute
import no.nav.openapi.utils.TestApplication
import org.junit.jupiter.api.Test

class OpenapiValidatorTest {
    @Test
    fun `Finds missing paths`() {
        TestApplication.withMockAuthServerAndTestApplication({
            simpleTestRoute()
        }) {
            this.application {
                val application = plugin(Routing).routesInApplication()
                val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.spechFilePath)
                openApiSpec `should contain the same paths as` application

            }
        }
    }
}
