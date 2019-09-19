package io.micronaut.docs.server.json

import com.fasterxml.jackson.core.JsonParseException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.*
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.LinkedHashMap
import java.util.Optional
import java.util.concurrent.CompletableFuture

/**
 * @author Graeme Rocher
 * @since 1.0
 */
// tag::class[]
@Controller("/people")
class PersonController {

    internal var inMemoryDatastore: MutableMap<String, Person> = LinkedHashMap()
    // end::class[]

    @Get
    fun index(): Collection<Person> {
        return inMemoryDatastore.values
    }

    @Get("/{name}")
    operator fun get(name: String): Maybe<Person> {
        return if (inMemoryDatastore.containsKey(name)) {
            Maybe.just(inMemoryDatastore[name])
        } else Maybe.empty()
    }

    // tag::single[]
    @Post
    fun save(@Body person: Single<Person>): Single<HttpResponse<Person>> { // <1>
        return person.map { p ->
            inMemoryDatastore[p.firstName] = p // <2>
            HttpResponse.created(p) // <3>
        }
    }
    // end::single[]


    @Post("/saveWithArgs")
    // tag::args[]
    fun save(firstName: String, lastName: String, age: Optional<Int>): HttpResponse<Person> {
        val p = Person(firstName, lastName)
        age.ifPresent(Consumer<Int> { p.age = it })
        inMemoryDatastore[p.firstName] = p
        return HttpResponse.created(p)
    }
    // end::args[]

    // tag::future[]
    fun save(@Body person: CompletableFuture<Person>): CompletableFuture<HttpResponse<Person>> {
        return person.thenApply { p ->
            inMemoryDatastore[p.firstName] = p
            HttpResponse.created(p)
        }
    }
    // end::future[]

    // tag::regular[]
    fun save(@Body person: Person): HttpResponse<Person> {
        inMemoryDatastore[person.firstName] = person
        return HttpResponse.created(person)
    }
    // end::regular[]

    // tag::localError[]
    @Error
    fun jsonError(request: HttpRequest<*>, jsonParseException: JsonParseException): HttpResponse<JsonError> { // <1>
        val error = JsonError("Invalid JSON: " + jsonParseException.message) // <2>
                .link(Link.SELF, Link.of(request.uri))

        return HttpResponse.status<JsonError>(HttpStatus.BAD_REQUEST, "Fix Your JSON")
                .body(error) // <3>
    }
    // end::localError[]


    @Get("/error")
    fun throwError(): String {
        throw RuntimeException("Something went wrong")
    }

    @Error // <1>
    // tag::globalError[]
    fun error(request: HttpRequest<*>, e: Throwable): HttpResponse<JsonError> {
        val error = JsonError("Bad Things Happened: " + e.message) // <2>
                .link(Link.SELF, Link.of(request.uri))

        return HttpResponse.serverError<JsonError>()
                .body(error) // <3>
    }
    // end::globalError[]

    @Error(status = HttpStatus.NOT_FOUND)
    // tag::statusError[]
    fun notFound(request: HttpRequest<*>): HttpResponse<JsonError> { // <1>
        val error = JsonError("Page Not Found") // <2>
                .link(Link.SELF, Link.of(request.uri))

        return HttpResponse.notFound<JsonError>()
                .body(error) // <3>
    }
    // end::statusError[]
}
