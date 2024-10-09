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
import nl.knaw.dans.lib.util.AbstractCommandLineApp;
import nl.knaw.dans.lib.util.PicocliVersionProvider;
import nl.knaw.dans.avexports.config.EasyConvertAvExportsConfig;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "easy-convert-av-exports",
         mixinStandardHelpOptions = true,
         versionProvider = PicocliVersionProvider.class,
         description = "Converts bags exported by easy-fedora-to-bag to bags with AV data")
@Slf4j
public class EasyConvertAvExports extends AbstractCommandLineApp<EasyConvertAvExportsConfig> {
    public static void main(String[] args) throws Exception {
        new EasyConvertAvExports().run(args);
    }

    public String getName() {
        return "Converts bags exported by easy-fedora-to-bag to bags with AV data";
    }

    @Override
    public void configureCommandLine(CommandLine commandLine, EasyConvertAvExportsConfig config) {
        // TODO: set up the API client, if applicable
        log.debug("Configuring command line");
        // TODO: add options and subcommands
    }
}
