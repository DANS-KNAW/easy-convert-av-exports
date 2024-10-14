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

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.reader.BagReader;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class BagUtilTest extends AbstractTestWithTestDir {

    @Test
    public void removePayloadManifestsForPath_should_remove_path_from_all_payload_manifests() throws Exception {
        Path testBag = testDir.resolve("bag");
        FileUtils.copyDirectory(Paths.get("src/test/resources/test-bags/bag1").toFile(), testBag.toFile());
        Bag bag = new BagReader().read(testBag);
        String localPath = "data/file1.txt";
        // Check precondition
        for (Manifest manifest : bag.getPayLoadManifests()) {
            assertThat(manifest.getFileToChecksumMap().containsKey(testBag.resolve(localPath))).isTrue();
        }
        BagUtil.removePayloadManifestsForPath(bag, localPath);

        for (Manifest manifest : bag.getPayLoadManifests()) {
            assertThat(manifest.getFileToChecksumMap().containsKey(testBag.resolve(localPath))).isFalse();
        }
    }

    @Test
    public void updatePayloadManifestsForPath_should_update_path_in_all_payload_manifests() throws Exception {
        Path testBag = testDir.resolve("bag");
        FileUtils.copyDirectory(Paths.get("src/test/resources/test-bags/bag1").toFile(), testBag.toFile());
        Bag bag = new BagReader().read(testBag);
        String localPath = "data/file1.txt";
        // Overwrite contents of file
        String newContents = "new contents";
        FileUtils.write(testBag.resolve(localPath).toFile(), newContents, "UTF-8");
        BagUtil.updatePayloadManifestsForPath(bag, localPath);

        for (Manifest manifest : bag.getPayLoadManifests()) {
            // Calculate expected checksum
            String expectedChecksum = new Hasher(testBag.resolve(localPath), manifest.getAlgorithm().toString()).getChecksum();
            assertThat(manifest.getFileToChecksumMap().get(testBag.resolve(localPath))).isEqualTo(expectedChecksum);
        }

    }
}
