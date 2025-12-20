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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryDataService implements DataService {

    private final Map<String, DataDomain> dataById = new ConcurrentHashMap<>();

    @Override
    public void insert(DataDomain data) {
        String dataId = data.id();
        if (dataById.containsKey(dataId)) {
            throw new DataConflictException(dataId);
        }
        dataById.put(dataId, data);
        log.info("Created new data - {}", data);
    }

    @Override
    public Optional<DataDomain> find(String dataId) {
        DataDomain data = dataById.get(dataId);
        if (data == null) {
            log.info("Not found data by id: {}", dataId);
            return Optional.empty();
        }
        log.info("Found data by id: {}", dataId);
        return Optional.of(data);
    }

    @Override
    public List<DataDomain> findAll() {
        return new ArrayList<>(dataById.values());
    }

    @Override
    public void remove(String dataId) {
        dataById.remove(dataId);
        log.info("Removed data by id: {}", dataId);
    }

    @Override
    public void removeAll() {
        dataById.clear();
        log.info("Removed all data");
    }

}
