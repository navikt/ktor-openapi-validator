# ktor-openapi-validator

Compares routes in an openapi spec with content of routes in one or more ktor modules

## Usage:

Assert that openapi spec is correcct

```kotlin
//with test application
val applicationRoutes = plugin(Routing).routesInApplication()
val openApiSpec = OpenApiSpec.fromJson("SimpleTestRoute.correctSpecFile")
assertDoesNotThrow {
    openApiSpec `should contain the same paths as` applicationRoutes
    openApiSpec `paths should have the same methods as` applicationRoutes
}
```

### Asserting and generate spec sceleton

The library can generate a sceleton for a new API with correct paths and methods,but it is nessescary to supply some
ekstra information in order for the spec to comply with openapi standards

```kotlin
SpecAssertionRecovery(openApiSpec, applicationRoutes).assertSpecAndRecover()
```

TODOs:

1. Unauthenticated routes
2. Unauthenticated paths
3. Write unauthenticated API to sceleton spec file
4. Authenticated routes
5. Authenticated paths
6. Write authenticated API to skeleton spec file
7. Write paths only to skeletonfile (easier to copy paste)

