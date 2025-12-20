/*
 * Copyright 2025-present The Concordile Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.cloud.contract.spec.Contract

import static org.springframework.http.HttpHeaders.LOCATION

Contract.make {
    description "Create data"
    request {
        method POST()
        url '/api/datum'
        headers { contentType(applicationJson()) }
        body(
                id: $(consumer(regex('id-.+')), producer('id-test')),
                data: $(consumer(regex('value-.+')), producer('value-test'))
        )
    }
    response {
        status CREATED()
        headers {
            header(
                    LOCATION,
                    $(
                            consumer('http://localhost/api/datum/id-test'),
                            producer("http://localhost/api/datum/${fromRequest().body('$.id')}")
                    )
            )
        }
    }
}
