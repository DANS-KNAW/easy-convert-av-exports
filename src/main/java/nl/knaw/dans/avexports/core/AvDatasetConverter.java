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
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Converts the bag(s) exported for one AV dataset to a bag with AV data.
 */
@Slf4j
@Builder
public class AvDatasetConverter {
    private final FedoraExports fedoraExports;
    private final Path outputDir;
    private final Sources sources;
    private final Path springfieldDir;

    public void convert() {
        checkEmpty(outputDir);
        createDirsIfNeeded(outputDir);
        try {
            for (String datasetId : fedoraExports.getDatasetIds()) {
                if (sources.hasSpringfieldFiles(datasetId)) {
                    log.info("Found Springfield files for dataset id {}", datasetId);
                    Path bagParentVersion2 = createVersion2BagIfNeeded(fedoraExports.getBagParentsForDatasetId(datasetId));
                    Path bagDir2 = fedoraExports.getBagDir(bagParentVersion2);
                    Bag bag2 = BagUtil.readBag(bagDir2);
                    log.debug(">>> Start processing bag parent {} >>>", bagParentVersion2);

                    FilesXml filesXml = new FilesXml(bagDir2.resolve("metadata/files.xml"));
                    for (String springfieldFileId : sources.getSpringfieldFileIdsFor(datasetId)) {
                        String springfieldFile = sources.getSpringfieldPathByFileId(springfieldFileId);
                        log.debug("Found Springfield file {} for file id {}", springfieldFile, springfieldFileId);
                        String originalFilePathInDataset = filesXml.getFilepathForFileId(springfieldFileId);
                        log.debug("Original file path in dataset: {}", originalFilePathInDataset);
                        String newFilePathInDataset = createNewFilepath(originalFilePathInDataset, springfieldFile);
                        log.debug("New file path in dataset: {}", newFilePathInDataset);
                        Path avFile = bagDir2.resolve(newFilePathInDataset);

                        // Replace the original file with the AV file
                        Files.delete(bagDir2.resolve(originalFilePathInDataset));
                        log.debug("Deleted original file {}", originalFilePathInDataset);
                        FileUtils.copyFile(springfieldDir.resolve(springfieldFile).toFile(), avFile.toFile());
                        log.debug("Copied Springfield file to {}", avFile);
                        filesXml.setFilepathForFileId(springfieldFileId, newFilePathInDataset);
                        log.debug("Updated files.xml with new file path {}", newFilePathInDataset);
                        BagUtil.removePayloadManifestEntriesForPath(bag2, originalFilePathInDataset);
                        BagUtil.updatePayloadManifestsForPath(bag2, newFilePathInDataset);
                        log.debug("Updated payload and tag manifests for new file path {}", newFilePathInDataset);

                        // Add subtitle files
                        Subtitles subtitles = new Subtitles(springfieldDir.resolve(springfieldFile));
                        for (String language : subtitles.getLanguages()) {
                            Path subtitleFileInSpringfieldDir = subtitles.getSubtitleFile(language);
                            String newSubtitleFilepath = createSubtitleFilepathFor(newFilePathInDataset, language);
                            FileUtils.copyFile(subtitleFileInSpringfieldDir.toFile(), bagDir2.resolve(newSubtitleFilepath).toFile());
                            log.debug("Copied subtitle file to {}", newSubtitleFilepath);
                            filesXml.addFile(newSubtitleFilepath, filesXml.getAccessibilityForFileId(springfieldFileId));
                            BagUtil.updatePayloadManifestsForPath(bag2, newSubtitleFilepath);
                            log.debug("Updated payload and tag manifests for subtitle file {}", newSubtitleFilepath);
                        }
                    }
                    // Remove empty files
                    removeEmptyFiles(bag2, filesXml);
                    log.debug("Removed empty files");
                    filesXml.write();
                    log.debug("Wrote updated files.xml");
                    BagUtil.updateTagManifestsForPaths(bag2, "metadata/files.xml", "bag-info.txt");
                    BagUtil.updatePayloadManifestChecksumsInTagManifests(bag2);
                    log.debug("Updated tag manifests");
                    // Write the manifests
                    BagUtil.writeBag(bag2);
                    log.debug("Wrote updated bag");
                    Files.move(bagParentVersion2, outputDir.resolve(bagParentVersion2.getFileName()));
                    log.debug("Moved version 2 bag to output directory");
                    log.debug("<<< Finished processing bag parent {} <<<", bagParentVersion2);
                }

                Path bagParentVersion1 = fedoraExports.getBagParentsForDatasetId(datasetId).get(0);
                log.debug(">>> Start processing bag parent {} >>>", bagParentVersion1);
                Path bagDir1 = fedoraExports.getBagDir(bagParentVersion1);
                Bag bag1 = BagUtil.readBag(bagDir1);
                FilesXml filesXml1 = new FilesXml(bagDir1.resolve("metadata/files.xml"));
                removeEmptyFiles(bag1, filesXml1);
                log.debug("Removed empty files from version 1 bag");
                filesXml1.write();
                log.debug("Wrote updated files.xml for version 1 bag");
                BagUtil.writeBag(bag1);
                log.debug("Wrote updated version 1 bag");
                Files.move(bagParentVersion1, outputDir.resolve(bagParentVersion1.getFileName()));
                log.debug("Moved version 1 bag to output directory");
                log.debug("<<< Finished processing bag parent {} <<<", bagParentVersion1);
            }
        }
        catch (IOException
               | ParserConfigurationException
               | SAXException
               | XPathExpressionException e) {
            throw new RuntimeException("Error converting AV dataset", e);
        }
    }

    private void checkEmpty(Path outputDir) {
        if (Files.exists(outputDir)) {
            try {
                try (Stream<Path> files = Files.list(outputDir)) {
                    if (files.findAny().isPresent()) {
                        throw new IllegalStateException("Output directory is not empty");
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException("Could not check if output directory is empty", e);
            }
        }
    }

    private void createDirsIfNeeded(Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not create output directory", e);
        }
    }

    private void removeEmptyFiles(Bag bag, FilesXml filesXml) {
        try {
            for (String fileId : filesXml.getFileIds()) {
                String filePath = filesXml.getFilepathForFileId(fileId);
                if (Files.size(bag.getRootDir().resolve(filePath)) == 0) {
                    Files.delete(bag.getRootDir().resolve(filePath));
                    BagUtil.removePayloadManifestEntriesForPath(bag, filePath);
                    filesXml.removeFile(fileId);
                }
            }
            BagUtil.updatePayloadManifestChecksumsInTagManifests(bag);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not remove empty files", e);
        }
    }

    private Path createVersion2BagIfNeeded(List<Path> bagParents) {
        try {
            if (bagParents.size() == 1) {
                Path version1BagDir = fedoraExports.getBagDir(bagParents.get(0));
                Path version2BagDir = fedoraExports.createNewBagPath();
                FileUtils.copyDirectory(version1BagDir.toFile(), version2BagDir.toFile());
                BagUtil.updateBagVersion(version2BagDir, version1BagDir);
                return version2BagDir.getParent();
            }
            else {
                return bagParents.get(1);
            }
        }
        catch (IOException e) {
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
    private String createNewFilepath(String originalPathInDataset, String springfieldFile) {
        Path po = Paths.get(originalPathInDataset);
        Path ps = Paths.get(springfieldFile);
        String springFieldExtension = ps.getFileName().toString().substring(ps.getFileName().toString().lastIndexOf('.'));
        String newFileName = replaceExtension(po.getFileName().toString(), springFieldExtension);
        return po.getParent().resolve(newFileName).toString();
    }

    private String createSubtitleFilepathFor(String avFileLocalPath, String language) {
        return stripExtension(avFileLocalPath) + "." + language + ".vtt";
    }

    private String stripExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private String replaceExtension(String fileName, String newExtension) {
        return fileName.substring(0, fileName.lastIndexOf('.')) + newExtension;
    }
}
