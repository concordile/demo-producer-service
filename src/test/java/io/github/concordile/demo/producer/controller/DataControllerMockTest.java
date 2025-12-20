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

package io.github.concordile.demo.producer.controller;

import io.github.concordile.demo.producer.converter.DataDomain2ResponseConverter;
import io.github.concordile.demo.producer.converter.DataRequest2DomainConverter;
import io.github.concordile.demo.producer.domain.DataDomain;
import io.github.concordile.demo.producer.exception.DataConflictException;
import io.github.concordile.demo.producer.payload.DataRequest;
import io.github.concordile.demo.producer.payload.DataResponse;
import io.github.concordile.demo.producer.service.DataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(DataController.class)
@AutoConfigureRestTestClient
class DataControllerMockTest {

    static final String API_PREFIX = "/api/datum";

    @Autowired
    RestTestClient client;

    @MockitoBean
    DataService service;
    @MockitoBean
    DataRequest2DomainConverter requestConverter;
    @MockitoBean
    DataDomain2ResponseConverter responseConverter;

    @Test
    void create_returns201_andLocation() {
        // Arrange
        var domain = new DataDomain("test-id", "test-data");
        given(requestConverter.convert(any(DataRequest.class))).willReturn(domain);
        // Act + Assert
        client.post()
                .uri(API_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "id": "test-id", "name": "test-data" }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "http://localhost/api/datum/test-id")
                .expectBody().isEmpty();

        // Verify
        verify(service).insert(eq(domain));
        verifyNoInteractions(responseConverter);
    }

    @Test
    void create_returns400_problemDetail_whenConflict() {
        // Arrange
        var domain = new DataDomain("test-id", "test-data");
        given(requestConverter.convert(any(DataRequest.class))).willReturn(domain);
        willThrow(new DataConflictException("test-id"))
                .given(service).insert(eq(domain));
        // Act + Assert
        client.post()
                .uri(API_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "id":"test-id", "name":"test-data" }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Data conflict")
                .jsonPath("$.detail").isEqualTo("Data conflict by id: test-id")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void get_returns200_whenFound() {
        // Arrange
        var domain = new DataDomain("test-id", "test-data");
        var response = new DataResponse("test-id", "test-data");
        given(service.find("test-id")).willReturn(Optional.of(domain));
        given(responseConverter.convert(domain)).willReturn(response);
        // Act + Assert
        client.get()
                .uri(API_PREFIX + "/{id}", "test-id")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("test-id")
                .jsonPath("$.data").isEqualTo("test-data");
    }

    @Test
    void get_returns404_whenMissing() {
        // Arrange
        given(service.find("unknown-id")).willReturn(Optional.empty());
        // Act + Assert
        client.get()
                .uri(API_PREFIX + "/{id}", "unknown-id")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
        // Verify
        verifyNoInteractions(responseConverter);
    }

    @Test
    void getAll_returns200_andArray() {
        // Arrange
        var d1 = new DataDomain("test-id1", "test-data1");
        var d2 = new DataDomain("test-id2", "test-data2");
        given(service.findAll()).willReturn(List.of(d1, d2));
        given(responseConverter.convert(d1)).willReturn(new DataResponse("test-id1", "test-data1"));
        given(responseConverter.convert(d2)).willReturn(new DataResponse("test-id2", "test-data2"));
        // Act + Assert
        client.get()
                .uri(API_PREFIX)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("test-id1")
                .jsonPath("$[1].id").isEqualTo("test-id2");
    }

    @Test
    void delete_returns204() {
        // Act + Assert
        client.delete()
                .uri(API_PREFIX + "/{id}", "test-id")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
        // Verify
        verify(service).remove("test-id");
        verifyNoInteractions(requestConverter, responseConverter);
    }

}
