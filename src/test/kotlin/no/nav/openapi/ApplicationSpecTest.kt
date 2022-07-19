package no.nav.openapi

import io.ktor.server.application.plugin
import io.ktor.server.routing.Routing
import no.nav.openapi.ApplicationSpec.Companion.routesInApplication
import no.nav.openapi.Path.OpenApiSpecPath
import no.nav.openapi.utils.SimpleTestRoute.expectedPaths
import no.nav.openapi.utils.withSimpleRoute
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ApplicationSpecTest {

    @Test
    fun `finds missing and superflous paths`() {
        createApplicationPaths(5).also { applicationPaths ->
            val applicationSpec = ApplicationSpec(applicationPaths)

            applicationPaths.convertToOpenApiSpecPath().also {
                assertEquals(0, applicationSpec.missingPaths(it).size)
                assertEquals(0, applicationSpec.superfluousPaths(it).size)
            }
            applicationPaths.convertToOpenApiSpecPath(ommit = 3).also {
                assertEquals(3, applicationSpec.missingPaths(it).size)
                assertEquals(0, applicationSpec.superfluousPaths(it).size)
            }
            applicationPaths.convertToOpenApiSpecPath(add = 2).also {
                assertEquals(0, applicationSpec.missingPaths(it).size)
                assertEquals(2, applicationSpec.superfluousPaths(it).size)
            }
            applicationPaths.convertToOpenApiSpecPath(ommit = 5, add = 2).also {
                assertEquals(5, applicationSpec.missingPaths(it).size)
                assertEquals(2, applicationSpec.superfluousPaths(it).size)
            }
        }
    }

    @Test
    fun `finds missing and superflous methods`() {
    }

    @Test
    fun `converts from Routes to applicationspec`() {
        withSimpleRoute {
            val applicationSpec = plugin(Routing).routesInApplication()
            assertEquals(2, applicationSpec.pathcount, "wrong pathcount")
            assertEquals(0, applicationSpec.missingPaths(expectedPaths).size, "wrong count of missing paths")
            assertEquals(0, applicationSpec.superfluousPaths(expectedPaths).size, "wrong count of superflous paths")
        }
    }
}

private fun List<Path.ApplicationPath>.convertToOpenApiSpecPath(ommit: Int = 0, add: Int = 0) =
    mutableListOf<OpenApiSpecPath>().also { openapiPaths ->
        this.subList(0, this.size - ommit).forEach {
            openapiPaths.add(OpenApiSpecPath(it.pathString, emptyList()))
        }
        for (number in 1..add) {
            openapiPaths.add(OpenApiSpecPath("/add/$number", emptyList()))
        }
    }

private fun createApplicationPaths(amount: Int): List<Path.ApplicationPath> {
    return mutableListOf<Path.ApplicationPath>().also {
        for (number in 0..amount) {
            it.add(Path.ApplicationPath("/test/$number", emptyList()))
        }
    }
}
