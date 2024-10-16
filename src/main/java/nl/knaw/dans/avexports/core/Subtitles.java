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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Subtitles {
    private final Map<String, Path> languageToSubtitles = new HashMap<>();

    private final Path springfieldFile;

    public Subtitles(Path springfieldFile) {
        this.springfieldFile = springfieldFile;
        Path greatGrandParent = springfieldFile.getParent().getParent().getParent();
        // Find all files in the grandparent directory that have extensions .srt or .vtt
        try (Stream<Path> files = Files.list(greatGrandParent)) {
            files.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().matches(".*\\.(srt|vtt)"))
                .forEach(p -> {
                    // Language is the two-letter code at the start of the filename preceding underscore char
                    String language = p.getFileName().toString().split("_")[0];
                    languageToSubtitles.put(language, p);
                });
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Set<String> getLanguages() {
        return languageToSubtitles.keySet();
    }

    public Path getSubtitleFile(String language) {
        return languageToSubtitles.get(language);
    }
}
