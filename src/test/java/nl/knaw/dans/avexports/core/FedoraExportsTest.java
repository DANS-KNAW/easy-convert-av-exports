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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class FedoraExportsTest {

    @Test
    public void ctor_should_find_all_dataset_ids_in_integration_input() throws Exception {
        FedoraExports fedoraExports = new FedoraExports(Paths.get("src/test/resources/integration/input-bags"));
        assertThat(fedoraExports.getDatasetIds()).containsExactlyInAnyOrder(
            "easy-dataset:218800",
            "easy-dataset:41418",
            "easy-dataset:121282",
            "easy-dataset:155170",
            "easy-dataset:112582"
        );
    }

    @Test
    public void ctor_should_find_one_bag_per_dataset_in_integration_input() throws Exception {
        FedoraExports fedoraExports = new FedoraExports(Paths.get("src/test/resources/integration/input-bags"));
        Path inputDir = Paths.get("src/test/resources/integration/input-bags");
        assertThat(fedoraExports.getBagParentsForDatasetId("easy-dataset:218800")).isEqualTo(Collections.singletonList(inputDir.resolve("54c97d8b-2eab-4718-ac17-e26eb8333987")));
        assertThat(fedoraExports.getBagParentsForDatasetId("easy-dataset:41418")).isEqualTo(Collections.singletonList(inputDir.resolve("7bf09491-54b4-436e-7f59-1027f54cbb0c")));
        assertThat(fedoraExports.getBagParentsForDatasetId("easy-dataset:121282")).isEqualTo(Collections.singletonList(inputDir.resolve("89e54b08-5f1f-452c-a551-0d35f75a3939")));
        assertThat(fedoraExports.getBagParentsForDatasetId("easy-dataset:155170")).isEqualTo(Collections.singletonList(inputDir.resolve("993ec2ee-b716-45c6-b9d1-7190f98a200a")));
        assertThat(fedoraExports.getBagParentsForDatasetId("easy-dataset:112582")).isEqualTo(Collections.singletonList(inputDir.resolve("eaa33307-4795-40a3-9051-e7d91a21838e")));
    }

    @Test
    public void ctor_should_find_two_bags_for_datasets_that_already_have_two_bags() {

    }

    @Test
    public void ctor_should_throw_exception_when_more_than_two_bags_found_for_dataset() {

    }

}
