package no.nav.openapi

import io.ktor.server.application.plugin
import io.ktor.server.routing.Routing
import no.nav.openapi.ApplicationSpec.Companion.routesInApplication
import no.nav.openapi.MethodAssertion.OpenApiMethodError
import no.nav.openapi.PathAssertion.OpenApiPathError
import no.nav.openapi.utils.SimpleTestRoute
import no.nav.openapi.utils.userDir
import no.nav.openapi.utils.withSimpleRoute
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OpenapiValidatorTest {
    @Test
    fun `happy path`() {
        withSimpleRoute {
            val applicationRoutes = plugin(Routing).routesInApplication()
            val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.correctSpecFile)
            assertDoesNotThrow {
                openApiSpec `should contain the same paths as` applicationRoutes
                openApiSpec `paths should have the same methods as` applicationRoutes
            }
        }
    }

    @Test
    fun `path errors in openapi spec`() {
        withSimpleRoute {
            val applicationRoutes = plugin(Routing).routesInApplication()
            val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.`specfile with 2 superflous and 1 missing path `)
            assertThrows<OpenApiPathError> {
                openApiSpec `should contain the same paths as` applicationRoutes
            }.also {
                assertEquals(1, it.missing, "Wrong missing count")
                assertEquals(2, it.superfluous, "Wrong superflous count")
            }
        }
    }

    @Test
    fun `method errors in openapi spec`() {
        withSimpleRoute {
            val applicationRoutes = plugin(Routing).routesInApplication()
            val openApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.`specfile with 3 missing and 2 superflous methods`)
            assertThrows<OpenApiMethodError> {
                openApiSpec `paths should have the same methods as` applicationRoutes
            }.also {
                assertEquals(3, it.missing, "wrong missing count")
                assertEquals(2, it.superfluous, "wrong superflous count")

                assertEquals(1, it.missingInPath(SimpleTestRoute.testPath).size, "wrong missing count for simple/test")
                assertEquals(2, it.superflousInPath(SimpleTestRoute.testPath).size, "superfluous count for simple/test")
                assertEquals(
                    2, it.missingInPath(SimpleTestRoute.testPath + "/{id}").size, "missing count for simple/test/{id}"
                )
                assertEquals(
                    0,
                    it.superflousInPath(SimpleTestRoute.testPath + "/{id}").size,
                    "superfluous count for simple/test{id}"
                )
            }

            val openApiSpec2 =
                OpenApiSpec.fromJson(SimpleTestRoute.`specfile with 1 missing and 2 superflous paths, 2 superflous and 2 missing methods`)
            assertThrows<OpenApiMethodError> {
                openApiSpec2 `paths should have the same methods as` applicationRoutes
            }
        }
    }

    @Test
    fun `performs recovery and generates correct paths`() {
        withSimpleRoute {
            val recoveryfilePath = "$userDir/build/tmp/openapi.json"
            val applicationRoutes = plugin(Routing).routesInApplication()
            val correctOpenApiSpec = OpenApiSpec.fromJson(SimpleTestRoute.correctSpecFile)
            val openApiSpecWithErrors =
                OpenApiSpec.fromJson(SimpleTestRoute.`specfile with 1 missing and 2 superflous paths, 2 superflous and 2 missing methods`)
            assertDoesNotThrow {
                SpecAssertionRecovery(correctOpenApiSpec, applicationRoutes)
                withRecovery(
                    SpecAssertionRecovery(correctOpenApiSpec, applicationRoutes),
                    { correctOpenApiSpec `should contain the same paths as` applicationRoutes },
                    { correctOpenApiSpec `paths should have the same methods as` applicationRoutes }
                )
            }
            assertThrows<MissingSpecContentError> {
                withRecovery(
                    SpecAssertionRecovery(openApiSpecWithErrors, applicationRoutes, recoveryfilePath),
                    { openApiSpecWithErrors `should contain the same paths as` applicationRoutes },
                    { openApiSpecWithErrors `paths should have the same methods as` applicationRoutes }
                )
            }
            assertDoesNotThrow {
                val updatedSpec = OpenApiSpec.fromJson(recoveryfilePath)
                updatedSpec `should contain the same paths as` applicationRoutes
                updatedSpec `paths should have the same methods as` applicationRoutes
            }
        }
    }
}
