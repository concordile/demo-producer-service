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

package io.github.concordile.demo.producer.service;

import io.github.concordile.demo.producer.domain.DataDomain;
import io.github.concordile.demo.producer.exception.DataConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InMemoryDataServiceTest {

    @BeforeEach
    void setUp(@Autowired DataService service) {
        service.removeAll();
    }

    @Test
    void should_insert(@Autowired DataService service) {
        String id = "new-id";
        String data = "new-value";
        var domain = new DataDomain(id, data);
        service.insert(domain);
        var optionalResult = service.find(id);
        assertTrue(optionalResult.isPresent());
        var result = optionalResult.get();
        assertEquals(id, result.id());
        assertEquals(data, result.data());
    }

    @Test
    void shouldNot_insert_due_conflict(@Autowired DataService service) {
        String id = "new-id";
        String data = "new-value";
        var domain = new DataDomain(id, data);
        service.insert(domain);
        var other = new DataDomain(id, "other-value");
        var exc = assertThrows(DataConflictException.class, () -> service.insert(other));
        assertEquals("Data conflict by id: " + id, exc.getMessage());
    }

    @Test
    void should_find(@Autowired DataService service) {
        String id = "new-id";
        String data = "new-value";
        var domain = new DataDomain(id, data);
        service.insert(domain);
        var optionalResult = service.find(id);
        assertTrue(optionalResult.isPresent());
        var result = optionalResult.get();
        assertEquals(id, result.id());
        assertEquals(data, result.data());
    }

    @Test
    void shouldNot_find(@Autowired DataService service) {
        String id = "unknown-id";
        var optionalResult = service.find(id);
        assertFalse(optionalResult.isPresent());
    }

    @Test
    void should_findAll(@Autowired DataService service) {
        String id1 = "new-id1";
        String data1 = "new-value1";
        var domain1 = new DataDomain(id1, data1);
        service.insert(domain1);
        String id2 = "new-id2";
        String data2 = "new-value2";
        var domain2 = new DataDomain(id2, data2);
        service.insert(domain2);
        var results = service.findAll();
        assertEquals(Set.of(domain1, domain2), new HashSet<>(results));
    }

    @Test
    void should_remove(@Autowired DataService service) {
        String id = "new-id";
        String data = "new-value";
        var domain = new DataDomain(id, data);
        service.insert(domain);
        assertTrue(service.find(id).isPresent());
        service.remove(id);
        assertFalse(service.find(id).isPresent());
    }

    @Test
    void should_removeAll(@Autowired DataService service) {
        String id = "new-id";
        String data = "new-value";
        var domain = new DataDomain(id, data);
        service.insert(domain);
        assertTrue(service.find(id).isPresent());
        service.removeAll();
        assertFalse(service.find(id).isPresent());
    }

}
