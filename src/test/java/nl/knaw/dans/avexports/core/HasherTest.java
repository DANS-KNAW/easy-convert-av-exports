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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class HasherTest extends AbstractTestWithTestDir {

    @Test
    public void getChecksum_should_return_correct_checksum_for_algorithm_md5() throws Exception {
        Path file = testDir.resolve("test.txt");
        FileUtils.write(file.toFile(), "test", "UTF-8");

        String checksum = new Hasher(file, "MD5").getChecksum();
        assertThat(checksum).isEqualTo("098f6bcd4621d373cade4e832627b4f6");
    }

    @Test
    public void getChecksum_should_return_correct_checksum_for_algorithm_sha1() throws Exception {
        Path file = testDir.resolve("test.txt");
        FileUtils.write(file.toFile(), "test", "UTF-8");

        String checksum = new Hasher(file, "SHA1").getChecksum();
        assertThat(checksum).isEqualTo("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
    }

}
