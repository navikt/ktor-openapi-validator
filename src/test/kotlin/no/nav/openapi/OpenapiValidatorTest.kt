package no.nav.openapi

import io.ktor.server.application.plugin
import io.ktor.server.routing.Routing
import no.nav.openapi.ApplicationSpec.Companion.routesInApplication
import no.nav.openapi.PathAssertion.OpenApiPathError
import no.nav.openapi.utils.SimpleTestRoute
import no.nav.openapi.utils.withSimpleRoute
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


class OpenapiValidatorTest {
    @Test
    fun `happy path`() {
        withSimpleRoute {
            val application = plugin(Routing).routesInApplication()
            val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.correctSpecFile)
            assertDoesNotThrow {
                openApiSpec `should contain the same paths as` application
            }
        }
    }

    @Test
    fun `errors in openapi spec`() {
        withSimpleRoute {
            val application = plugin(Routing).routesInApplication()
            val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.spechFilePathWith1MissingAnd2superflous)
            assertThrows<OpenApiPathError> {
                openApiSpec `should contain the same paths as` application
            }.also {
                assertEquals(1, it.missing, "Wrong missing count")
                assertEquals(2, it.superflous, "Wrong superflous count")
            }
        }
    }

    @Test
    fun withRecovery() {
        withSimpleRoute {
            val application = plugin(Routing).routesInApplication()
            val correctOpenApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.correctSpecFile)
            val openApiSpecWithErrors = OpenApiSpec.fromJson(SimpleTestRoute.spechFilePathWith1MissingAnd2superflous)
            withRecovery(SpecAssertionRecovery(correctOpenApiSpec, application)) {
                assertDoesNotThrow { correctOpenApiSpec `should contain the same paths as` application }
            }
            assertThrows<MissingSpecContentError> {
                withRecovery(SpecAssertionRecovery(openApiSpecWithErrors, application)) {
                    openApiSpecWithErrors `should contain the same paths as` application
                }
            }
            //TODO: Legg til missing/superflous
        }
    }
}

