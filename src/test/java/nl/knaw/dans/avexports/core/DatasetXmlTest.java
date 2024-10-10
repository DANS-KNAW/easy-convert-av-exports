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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatasetXmlTest {
    private static final String namespaceBindings = "xmlns:ddm=\"http://easy.dans.knaw.nl/schemas/md/ddm/\" "
        + "xmlns:dcx-dai=\"http://easy.dans.knaw.nl/schemas/dcx/dai/\" xmlns:dct=\"http://purl.org/dc/terms/\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"";

    @Test
    public void getDatasetId_should_return_the_dataset_id() throws Exception {
        String id = "easy-dataset:12345";
        DatasetXml datasetXml = new DatasetXml(String.format(
            "<ddm:DDM " + namespaceBindings + ">" +
                " <ddm:profile>\n" +
                " </ddm:profile>\n" +
                " <ddm:dcmiMetadata>\n" +
                "    <dct:identifier xsi:type=\"id-type:EASY2\">%s</dct:identifier>\n" +
                " </ddm:dcmiMetadata>\n" +
                "</ddm:DDM>", id));

        assertThat(datasetXml.getDatasetId()).isEqualTo(id);
    }

}
