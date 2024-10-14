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

/**
 * Represents the exports from Fedora. The exports are expected to be in the following structure:
 * <pre>
 *     exports-dir
 *        bag-parent-1 (a UUID)
 *           bag-1
 *        bag-parent-2 (a UUID)
 *           bag-2
 * </pre>
 * The bag-parents will later be converted to deposits by easy-convert-bag-to-deposit. Each bag contains a dataset.xml with a datasetId (e.g., easy-dataset:12345). There are one ore two bags for each
 * datasetId. The bags are versioned, so there can be a version 1 and a version 2 bag for the same datasetId. This class provides a way to find the bags for a datasetId.
 */
@Slf4j
public class FedoraExports {
    private final Path inputDir;
    private final Map<String, List<Path>> idToBagParents = new HashMap<>();

    public FedoraExports(Path inputDir) throws IOException {
        this.inputDir = inputDir;
        buildIdToBagPaths();
    }

    public List<Path> getBagParentsForDatasetId(String datasetId) {
        return idToBagParents.get(datasetId);
    }

    public Set<String> getDatasetIds() {
        return idToBagParents.keySet();
    }

    private void buildIdToBagPaths() throws IOException {
        log.info("Building dataset id to bag paths map");
        try (Stream<Path> bagParents = Files.list(inputDir)) {
            bagParents.forEach(bagParent -> {
                String datasetId = findDatasetId(bagParent);
                List<Path> bagParentsForDatasetId = idToBagParents.getOrDefault(datasetId, new ArrayList<>());
                // TODO: insert as first element if version 1
                bagParentsForDatasetId.add(bagParent);
                idToBagParents.put(datasetId, bagParentsForDatasetId);
                if (idToBagParents.get(datasetId).size() > 2) {
                    throw new IllegalStateException("More than 2 bags found for dataset id " + datasetId);
                }
            });
        }
        log.info("Found {} datasets for {} bags", idToBagParents.size(), idToBagParents.values().stream().mapToLong(List::size).sum());
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

    public Path getBagDir(Path bagParent) {
        try (Stream<Path> paths = Files.list(bagParent).filter(Files::isDirectory).filter(f -> !f.equals(bagParent))) {
            return paths.findFirst().orElseThrow(() -> new IllegalStateException("No directories found in " + bagParent));
        }
        catch (IOException e) {
            throw new RuntimeException("Error while reading " + bagParent, e);
        }
    }
}
