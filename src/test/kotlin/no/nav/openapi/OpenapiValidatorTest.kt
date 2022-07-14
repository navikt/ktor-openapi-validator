package no.nav.openapi

import io.ktor.server.application.plugin
import io.ktor.server.routing.Routing
import no.nav.openapi.PathAssertion.OpenApiPathError
import no.nav.openapi.utils.SimpleTestRoute
import no.nav.openapi.utils.SimpleTestRoute.simpleTestRoute
import no.nav.openapi.utils.TestApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OpenapiValidatorTest {
    @Test
    fun `Finds missing paths`() {
        TestApplication.withMockAuthServerAndTestApplication({
            simpleTestRoute()
        }) {
            this.application {
                val application = plugin(Routing).routesInApplication()
                val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.spechFilePath)
                assertThrows<OpenApiPathError> { openApiSpec `should contain the same paths as` application }.also {
                    println(it.message)
                    assertEquals(1,it.missing)
                    assertEquals(0, it.superflous)
                }

            }
        }
    }
}
