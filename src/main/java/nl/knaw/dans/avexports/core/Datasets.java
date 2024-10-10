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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class Datasets {
    private final Path inputDir;
    private final Map<String, List<Path>> idToBagPaths = new HashMap<>();

    public Datasets(Path inputDir) throws IOException {
        this.inputDir = inputDir;
        buildIdToBagPaths();
    }

    private void buildIdToBagPaths() throws IOException {
        log.info("Building dataset id to bag paths map");
        try (Stream<Path> bagParents = Files.list(inputDir)) {
            bagParents.forEach(bagParent -> {
                String datasetId = findDatasetId(bagParent);
                List<Path> bags = idToBagPaths.getOrDefault(datasetId, new ArrayList<>());
                idToBagPaths.put(datasetId, bags);
                if (idToBagPaths.get(datasetId).size() > 2) {
                    throw new IllegalStateException("More than 2 bags found for dataset id " + datasetId);
                }
            });
        }
        log.info("Found {} datasets for {} bags", idToBagPaths.size(), idToBagPaths.values().stream().mapToLong(List::size).sum());
    }

    private String findDatasetId(Path bagParent) {
        checkOneSubdirectory(bagParent);
        Path bagDir = getBagDir(bagParent);
        Path datasetXml = bagDir.resolve("metadata/dataset.xml");
        if (Files.exists(datasetXml)) {
            try {
                return new DatasetXml(datasetXml).getDatasetId();
            }
            catch (Exception e) {
                throw new RuntimeException("Error while reading dataset.xml in " + bagDir, e);
            }
        }
        return null;
    }

    private void checkOneSubdirectory(Path bagParent) {
        try (Stream<Path> bagParentStream = Files.list(bagParent).filter(Files::isDirectory)) {
            long count = bagParentStream.count();
            if (count > 1) {
                throw new IllegalStateException("More than one dir found in " + bagParent);
            }
            else if (count == 0) {
                throw new IllegalStateException("No dirs found in " + bagParent);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error while reading " + bagParent, e);
        }
    }

    private Path getBagDir(Path bagParent) {
        try (Stream<Path> paths = Files.list(bagParent).filter(Files::isDirectory).filter(f -> !f.equals(bagParent))) {
            return paths.findFirst().orElseThrow(() -> new IllegalStateException("No directories found in " + bagParent));
        }
        catch (IOException e) {
            throw new RuntimeException("Error while reading " + bagParent, e);
        }
    }

    public List<Path> getBagsForDataset(String datasetId) {
        return idToBagPaths.get(datasetId);
    }

    public Set<String> getDatasetIds() {
        return idToBagPaths.keySet();
    }
}
