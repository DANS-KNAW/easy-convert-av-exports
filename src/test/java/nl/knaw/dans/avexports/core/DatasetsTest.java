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

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DatasetsTest {

    @Test
    public void ctor_should_find_all_dataset_ids_in_input() throws Exception {
        Datasets datasets = new Datasets(Paths.get("src/test/resources/integration/input-bags"));
        assertThat(datasets.getDatasetIds()).containsExactlyInAnyOrder(
            "easy-dataset:218800",
            "easy-dataset:41418",
            "easy-dataset:121282",
            "easy-dataset:155170",
            "easy-dataset:112582"
        );
    }

}
