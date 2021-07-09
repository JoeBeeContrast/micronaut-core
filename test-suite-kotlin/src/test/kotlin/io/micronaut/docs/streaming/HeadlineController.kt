/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.docs.streaming

// tag::imports[]
import io.micronaut.http.MediaType.APPLICATION_JSON_STREAM
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Flowable
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit.SECONDS
// end::imports[]

@Controller("/streaming")
class HeadlineController {

    // tag::streaming[]
    @Get(value = "/headlines", processes = [APPLICATION_JSON_STREAM]) // <1>
    internal fun streamHeadlines(): Flowable<Headline> {
        return Flowable.fromCallable { // <2>
            val headline = Headline()
            headline.text = "Latest Headline at ${ZonedDateTime.now()}"
            headline
        }.repeat(100) // <3>
         .delay(1, SECONDS) // <4>
    }
    // end::streaming[]
}
