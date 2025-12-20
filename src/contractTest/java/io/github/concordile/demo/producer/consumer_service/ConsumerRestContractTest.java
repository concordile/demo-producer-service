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

package io.github.concordile.demo.producer.consumer_service;

import io.github.concordile.demo.producer.domain.DataDomain;
import io.github.concordile.demo.producer.exception.DataConflictException;
import io.github.concordile.demo.producer.service.DataService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DirtiesContext
@AutoConfigureMessageVerifier
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ConsumerRestContractTest {

    @MockitoBean
    DataService dataService;

    @BeforeEach
    void setupWebApplicationContext(@Autowired WebApplicationContext context) {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @BeforeEach
    void mockDataService() {
        doNothing().when(dataService).insert(any());
        doThrow(new DataConflictException("Already exists"))
                .when(dataService)
                .insert(argThat(d -> "id-conflict".equals(d.id())));
        when(dataService.find(any())).thenReturn(Optional.of(
                new DataDomain("id-test", "value-test")
        ));
        when(dataService.find("id-missing")).thenReturn(Optional.empty());
        when(dataService.findAll()).thenReturn(List.of(
                new DataDomain("id-1", "value-1"),
                new DataDomain("id-2", "value-2")
        ));
    }

}
