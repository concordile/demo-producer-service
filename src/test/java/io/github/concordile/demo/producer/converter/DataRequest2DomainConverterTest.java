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

package io.github.concordile.demo.producer.converter;

import io.github.concordile.demo.producer.domain.DataDomain;
import io.github.concordile.demo.producer.payload.DataRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = DataRequest2DomainConverterImpl.class)
class DataRequest2DomainConverterTest {

    @Test
    void should_convert(@Autowired DataRequest2DomainConverter converter) {
        String id = "test-id";
        String data = "test-data";
        DataDomain domain = converter.convert(new DataRequest(id, data));
        assertEquals(new DataDomain(id, data), domain);
    }

}
