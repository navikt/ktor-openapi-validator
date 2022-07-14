package no.nav.openapi

import no.nav.openapi.Path.OpenApiSpecPath
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
            applicationPaths.convertToOpenApiSpecPath(ommit=5,add = 2).also {
                assertEquals(5, applicationSpec.missingPaths(it).size)
                assertEquals(2, applicationSpec.superfluousPaths(it).size)
            }

        }
    }

    @Test
    fun `missingMethods`() {
    }

    @Test
    fun superfluousMethods() {
    }
}

private fun List<Path.ApplicationPath>.convertToOpenApiSpecPath(ommit: Int = 0, add: Int = 0) =
    mutableListOf<OpenApiSpecPath>().also { openapiPaths ->
        this.subList(0, this.size - ommit).forEach {
            openapiPaths.add(OpenApiSpecPath(it.value, emptyList()))
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