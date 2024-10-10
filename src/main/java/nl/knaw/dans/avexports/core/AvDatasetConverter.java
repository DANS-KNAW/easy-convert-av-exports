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

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * Converts the bag(s) exported for one AV dataset to a bag with AV data.
 */
@Slf4j
@Builder
public class AvDatasetConverter {
    private final Path inputDir;
    private final Path outputDir;
    private final Sources sources;

    public void convert() throws Exception {
        Datasets datasets = new Datasets(inputDir);

        for (String easyDatasetId: datasets.getDatasetIds()) {
            if (sources.hasSpringfieldFiles(easyDatasetId)) {
               if (datasets.getBagsForDataset(easyDatasetId).size() == 1) {
                   log.info("Only one bag found for dataset id {}. Creating second version bag...", easyDatasetId);
               }
               for (String springfieldFileId: sources.getSpringfieldPathsByDatasetId(easyDatasetId)) {
                   // Copy files to outputDir


                   // Modify entry in files.xml

               }

            }

            // Remove empty files


            // Update bag manifests

        }
    }
}
