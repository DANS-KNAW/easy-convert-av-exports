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
import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import nl.knaw.dans.bagit.exceptions.UnparsableVersionException;
import nl.knaw.dans.bagit.exceptions.UnsupportedAlgorithmException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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
    private final Path springfieldDir;

    public void convert() throws Exception {
        FedoraExports fedoraExports = new FedoraExports(inputDir);

        for (String easyDatasetId : fedoraExports.getDatasetIds()) {
            if (sources.hasSpringfieldFiles(easyDatasetId)) {
                log.info("Found Springfield files for dataset id {}", easyDatasetId);
                Path bagParentVersion2 = createVersion2BagIfNeeded(fedoraExports.getBagParentsForDatasetId(easyDatasetId));
                Path bagDir = fedoraExports.getBagDir(bagParentVersion2);
                Bag bag2 = BagUtil.getBag(bagDir);

                FilesXml filesXml = new FilesXml(bagParentVersion2.resolve("metadata/files.xml"));
                for (String springfieldFileId : sources.getSpringfieldFileIdsFor(easyDatasetId)) {
                    String springfieldFile = sources.getSpringfieldPathByFileId(springfieldFileId);
                    String originalFilePathInDataset = filesXml.getFilepathForFileId(springfieldFileId);
                    String newFilePathInDataset = createNewFilePath(originalFilePathInDataset, springfieldFile);
                    Path avFile = bagDir.resolve(newFilePathInDataset);
                    FileUtils.copyFile(springfieldDir.resolve(springfieldFile).toFile(), avFile.toFile());
                    filesXml.setFilepathForFileId(springfieldFileId, newFilePathInDataset);
                    BagUtil.removePayloadManifestsForPath(bag2, originalFilePathInDataset);
                    BagUtil.updatePayloadManifestsForPath(bag2, newFilePathInDataset);
                }
                filesXml.write();

                // TODO: update tagmanifest for files.xml, payload manifests and bag-info.txt
            }

            // Remove empty files
            for (Path bagParent : fedoraExports.getBagParentsForDatasetId(easyDatasetId)) {
                //
            }
        }
    }

    private Path createVersion2BagIfNeeded(List<Path> bagParents) {
        try {
            if (bagParents.size() == 1) {
                Path version2Bag = outputDir.resolve(UUID.randomUUID().toString());
                FileUtils.copyDirectory(bagParents.get(0).toFile(), version2Bag.toFile());
                BagUtil.updateBagVersion(version2Bag, bagParents.get(0));
                return version2Bag;
            }
            else {
                return bagParents.get(1);
            }
        }
        catch (IOException | UnparsableVersionException | MaliciousPathException | UnsupportedAlgorithmException | InvalidBagitFileFormatException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Could not create version 2 bag", e);
        }
    }

    /**
     * Creates a new file path for the AV file based on the original file path and the extension of the Springfield file.
     *
     * @param originalPathInDataset the original file path
     * @param springfieldFile       the path to file in the Springfield directory
     * @return the new file path
     */
    private String createNewFilePath(String originalPathInDataset, String springfieldFile) {
        Path po = Paths.get(originalPathInDataset);
        Path ps = Paths.get(springfieldFile);
        String springFieldExtension = ps.getFileName().toString().substring(ps.getFileName().toString().lastIndexOf('.'));
        String newFileName = po.getFileName().toString().substring(0, po.getFileName().toString().lastIndexOf('.')) + springFieldExtension;
        return po.getParent().resolve(newFileName).toString();
    }
}
