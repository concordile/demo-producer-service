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

Contract.make {
    description "Get data by id when it exists"
    request {
        method GET()
        urlPath $(
                consumer(regex('/api/datum/id-.+')),
                producer('/api/datum/id-test')
        )
    }
    response {
        status OK()
        headers { contentType(applicationJson()) }
        body(
                id: $(consumer('id-test'), producer(regex('id-.+'))),
                data: $(consumer('value-test'), producer(anyNonBlankString()))
        )
    }
}
