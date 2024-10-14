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

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor
public class Hasher {
    private final Path file;
    private final String algorithm;

    public String getChecksum() {
        try {
            byte[] fileBytes = Files.readAllBytes(file);
            switch (algorithm.toUpperCase()) {
                case "MD5":
                    return DigestUtils.md5Hex(fileBytes);
                case "SHA1":
                    return DigestUtils.sha1Hex(fileBytes);
                case "SHA256":
                    return DigestUtils.sha256Hex(fileBytes);
                case "SHA512":
                    return DigestUtils.sha512Hex(fileBytes);
                default:
                    throw new UnsupportedOperationException("Unsupported algorithm: " + algorithm);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading file for checksum calculation", e);
        }
    }
}
