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
import io.github.concordile.demo.producer.exception.DataConflictException;
import io.github.concordile.demo.producer.payload.DataRequest;
import io.github.concordile.demo.producer.payload.DataResponse;
import io.github.concordile.demo.producer.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/datum")
public class DataController {

    private final DataService service;
    private final DataRequest2DomainConverter requestConverter;
    private final DataDomain2ResponseConverter responseConverter;

    @PostMapping
    public ResponseEntity<DataResponse> create(@RequestBody DataRequest request) {
        var data = requestConverter.convert(request);
        service.insert(data);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(data.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{dataId}")
    public ResponseEntity<DataResponse> get(@PathVariable String dataId) {
        return service.find(dataId)
                .map(responseConverter::convert)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DataResponse>> getAll() {
        return ResponseEntity.ok(service.findAll().stream()
                .map(responseConverter::convert)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{dataId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String dataId) {
        service.remove(dataId);
    }

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ProblemDetail> handleDataConflict(DataConflictException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail body = ProblemDetail.forStatus(status);
        body.setTitle("Data conflict");
        body.setDetail(ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

}
