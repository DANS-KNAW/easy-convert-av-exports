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
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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
        FedoraExports fedoraExports = new FedoraExports(inputDir);

        for (String easyDatasetId : fedoraExports.getDatasetIds()) {
            if (sources.hasSpringfieldFiles(easyDatasetId)) {
                log.info("Found Springfield files for dataset id {}", easyDatasetId);
                Path bagParentVersion2 = createVersion2BagIfNeeded(fedoraExports.getBagParentsForDatasetId(easyDatasetId));
                FilesXml filesXml = new FilesXml(bagParentVersion2.resolve("metadata/files.xml"));
                for (String springfieldFileId : sources.getSpringfieldPathsByDatasetId(easyDatasetId)) {
                    Path springfieldFile = sources.getSpringfieldPathByFileId(springfieldFileId);
                    Path originalFilePath = filesXml.getFilepathForFileId(springfieldFileId);
                    Path avFile = outputDir.resolve(createNewFilePath(originalFilePath, springfieldFile).getFileName().toString());
                    FileUtils.copyFile(springfieldFile.toFile(), avFile.toFile());
                    filesXml.setFilepathForFileId(springfieldFileId, Paths.get(avFile.getFileName().toString()));
                    // TODO: change the filepath also in the payload manifests.
                }
                filesXml.write();
            }

            // Remove empty files

            // Update bag manifests for both bags

        }
    }

    private Path createVersion2BagIfNeeded(List<Path> bagParents) throws IOException {
        if (bagParents.size() == 1) {
            Path version2Bag = outputDir.resolve(UUID.randomUUID().toString());
            FileUtils.copyDirectory(bagParents.get(0).toFile(), version2Bag.toFile());
            String bagInfo = FileUtils.readFileToString(version2Bag.resolve("bag-info.txt").toFile(), StandardCharsets.UTF_8);
            bagInfo += "Is-Version-Of: " + bagParents.get(0).getFileName().toString() + "\n";
            FileUtils.write(version2Bag.resolve("bag-info.txt").toFile(), bagInfo, StandardCharsets.UTF_8);
            return version2Bag;
        }
        else {
            return bagParents.get(1);
        }
    }

    /**
     * Creates a new file path for the AV file based on the original file path and the extension of the Springfield file.
     *
     * @param filePath        the original file path
     * @param springFieldPath the path to file in the Springfield directory
     * @return the new file path
     */
    private Path createNewFilePath(Path filePath, Path springFieldPath) {
        String springFieldExtension = springFieldPath.getFileName().toString().substring(springFieldPath.getFileName().toString().lastIndexOf('.'));
        String newFileName = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf('.')) + springFieldExtension;
        return filePath.getParent().resolve(newFileName);
    }
}
