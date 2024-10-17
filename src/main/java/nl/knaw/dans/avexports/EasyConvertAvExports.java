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

package nl.knaw.dans.avexports;

import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.avexports.config.EasyConvertAvExportsConfig;
import nl.knaw.dans.avexports.core.AvDatasetConverter;
import nl.knaw.dans.avexports.core.FedoraExports;
import nl.knaw.dans.avexports.core.Sources;
import nl.knaw.dans.lib.util.PicocliVersionProvider;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.copy;

@Command(name = "easy-convert-av-exports",
         mixinStandardHelpOptions = true,
         versionProvider = PicocliVersionProvider.class,
         description = "Converts bags exported by easy-fedora-to-bag to bags with AV data")
@Slf4j
public class EasyConvertAvExports extends AbstractCommandLineAppJava8<EasyConvertAvExportsConfig> {
    public static void main(String[] args) throws Exception {
        new EasyConvertAvExports().run(args);
    }

    @Parameters(index = "0", description = "Input directory containing the bags exported by easy-fedora-to-bag")
    private Path inputDir;

    @Parameters(index = "1", description = "Output directory for the bags with AV data")
    private Path outputDir;

    @Option(names = { "-m", "--move" },
            description = "Move the input to the staging directory instead of copying it")
    private boolean move;

    @Option(names = { "-f", "--fail-fast" },
            description = "Fail run on first error")
    private boolean failFast;

    private Path stagingDir;

    private final AvDatasetConverter.AvDatasetConverterBuilder builder = AvDatasetConverter.builder();

    public String getName() {
        return "Converts bags exported by easy-fedora-to-bag to bags with AV data";
    }

    @Override
    public void configureCommandLine(CommandLine commandLine, EasyConvertAvExportsConfig config) {
        log.debug("Reading configuration sources from {}", config.getSources());
        try {
            builder.sources(new Sources(config.getSources().getPath()))
                .springfieldDir(config.getSources().getSpringfieldDir());
            stagingDir = config.getStagingDir();
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading sources configuration", e);
        }
    }

    @Override
    public Integer call() {
        try {
            FedoraExports fedoraExports;
            if (move) {
                fedoraExports = new FedoraExports(inputDir);
            }
            else {
                log.debug("Recreating staging dir {}", stagingDir);
                FileUtils.deleteDirectory(stagingDir.toFile());
                log.info("Copying input to staging dir {}", stagingDir);
                FileUtils.copyDirectory(inputDir.toFile(), stagingDir.toFile());
                fedoraExports = new FedoraExports(stagingDir);
            }
            builder
                .fedoraExports(fedoraExports)
                .outputDir(outputDir)
                .failFast(failFast)
                .build()
                .convert();
            return 0;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
