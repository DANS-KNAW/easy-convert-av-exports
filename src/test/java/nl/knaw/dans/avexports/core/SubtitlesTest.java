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

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class SubtitlesTest {

    @Test
    public void getLanguages_should_find_all_languages() {
        Subtitles subtitles = new Subtitles(Paths.get("src/test/resources/integration/springfield/domain/dans/user/Caleidoscoop_Film/video/31/rawvideo/2/GV_CaleidoscoopFilm_ingekwartierd_08.mp4"));
        assertThat(subtitles.getLanguages()).containsExactlyInAnyOrder("en", "nl");
    }

    @Test
    public void getSubtitleFile_should_return_path_to_subtitle_file() {
        Subtitles subtitles = new Subtitles(Paths.get("src/test/resources/integration/springfield/domain/dans/user/Caleidoscoop_Film/video/31/rawvideo/2/GV_CaleidoscoopFilm_ingekwartierd_08.mp4"));
        assertThat(subtitles.getSubtitleFile("en")).isEqualTo(
            Paths.get("src/test/resources/integration/springfield/domain/dans/user/Caleidoscoop_Film/video/31/en_GV_CaleidoscoopFilm_ingekwartierd_08_conversation.srt.vtt"));
        assertThat(subtitles.getSubtitleFile("nl")).isEqualTo(
            Paths.get("src/test/resources/integration/springfield/domain/dans/user/Caleidoscoop_Film/video/31/nl_GV_CaleidoscoopFilm_ingekwartierd_08_conversation.srt.vtt"));
    }

}
