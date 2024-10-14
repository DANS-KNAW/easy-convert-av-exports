/*
 * Copyright (C) 2024 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.avexports.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the sources.csv file, which contains the mapping between easy_file_id and path_in_springfield_dir and between easy_dataset_id and easy_file_id.
 */
@Slf4j
public class Sources {
    private final Map<String, String> fileIdToSpringfieldPath = new HashMap<>();

    private final Map<String, Set<String>> datasetIdToSpringfieldFileIds = new HashMap<>();

    public Sources(Path sourcesCsv) throws IOException {
        log.info("Reading sources from {}", sourcesCsv);
        try (CSVParser csvParser = CSVParser.parse(sourcesCsv.toFile(), StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader())) {
            for (CSVRecord csvRecord : csvParser) {
                fileIdToSpringfieldPath.put(csvRecord.get("easy_file_id"), csvRecord.get("path_in_springfield_dir"));
                datasetIdToSpringfieldFileIds.computeIfAbsent(csvRecord.get("easy_dataset_id"), k -> new HashSet<>()).add(csvRecord.get("easy_file_id"));
            }
        }
        log.info("Read {} rows from {}", fileIdToSpringfieldPath.size(), sourcesCsv);
    }

    public String getSpringfieldPathByFileId(String fileId) {
        return fileIdToSpringfieldPath.get(fileId);
    }

    public Set<String> getSpringfieldFileIdsFor(String easyDatasetId) {
        return datasetIdToSpringfieldFileIds.get(easyDatasetId);
    }

    public boolean hasSpringfieldFiles(String easyDatasetId) {
        return datasetIdToSpringfieldFileIds.containsKey(easyDatasetId);
    }
}
