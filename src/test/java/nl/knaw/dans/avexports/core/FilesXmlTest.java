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
import org.w3c.dom.Document;

import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FilesXmlTest extends AbstractTestWithTestDir {
    private static final String namespaceBindings =
        "xmlns=\"http://easy.dans.knaw.nl/schemas/bag/metadata/files/\" xmlns:dct=\"http://purl.org/dc/terms/\"";

    @Test
    public void getFilepathForFileId_should_return_the_filepath() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        assertThat(filesXml.getFilepathForFileId("easy-file:1")).isEqualTo("path/to/file");
    }

    @Test
    public void getFilepathForFileId_should_return_the_filepath_when_multiple_files() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file1\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + " <file filepath=\"path/to/file2\">"
            + "  <dct:identifier>easy-file:2</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        assertThat(filesXml.getFilepathForFileId("easy-file:2")).isEqualTo("path/to/file2");
    }

    @Test
    public void getFilepathForFileId_should_throw_exception_when_no_file_found() throws Exception {
        String xml = "<files " + namespaceBindings + "></files>";
        FilesXml filesXml = new FilesXml(xml, null);
        assertThatThrownBy(() -> filesXml.getFilepathForFileId("easy-file:1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No file with id easy-file:1 found in files.xml");
    }

    @Test
    public void setFilepathForFileId_should_set_the_filepath() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        filesXml.setFilepathForFileId("easy-file:1", "new/path/to/file");
        assertThat(filesXml.getFilepathForFileId("easy-file:1")).isEqualTo("new/path/to/file");
    }

    @Test
    public void write_should_write_the_xml() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, testDir.resolve("files.xml"));
        filesXml.setFilepathForFileId("easy-file:1", "new/path/to/file");
        filesXml.write();

        String actual = new String(Files.readAllBytes(testDir.resolve("files.xml")));
        assertThat(actual).contains("filepath=\"new/path/to/file\"");
    }

    @Test
    public void getFileIds_should_return_the_file_ids() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file1\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + " <file filepath=\"path/to-file:2\">"
            + "  <dct:identifier>easy-file:2</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        assertThat(filesXml.getFileIds()).containsExactly("easy-file:1", "easy-file:2");
    }

    @Test
    public void getFileIds_should_return_empty_list_when_no_files() throws Exception {
        String xml = "<files " + namespaceBindings + "></files>";
        FilesXml filesXml = new FilesXml(xml, null);
        assertThat(filesXml.getFileIds()).isEmpty();
    }

    @Test
    public void removeFile_should_remove_the_file() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file1\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + " <file filepath=\"path/to-file:2\">"
            + "  <dct:identifier>easy-file:2</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        filesXml.removeFile("easy-file:1");
        assertThat(filesXml.getFileIds()).containsExactly("easy-file:2");
    }

    @Test
    public void removeFile_should_throw_exception_when_no_file_found() throws Exception {
        String xml = "<files " + namespaceBindings + "></files>";
        FilesXml filesXml = new FilesXml(xml, null);
        assertThatThrownBy(() -> filesXml.removeFile("easy-file:1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No file with id easy-file:1 found in files.xml");
    }

    @Test
    public void getAccessibilityForFileId_should_return_the_accessibility() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file\">"
            + "  <accessibleToRights>ANONYMOUS</accessibleToRights>"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        assertThat(filesXml.getAccessibilityForFileId("easy-file:1")).isEqualTo("ANONYMOUS");
    }

    @Test
    public void getAccessibilityForFileId_should_throw_exception_when_no_file_found() throws Exception {
        String xml = "<files " + namespaceBindings + "></files>";
        FilesXml filesXml = new FilesXml(xml, null);
        assertThatThrownBy(() -> filesXml.getAccessibilityForFileId("easy-file:1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No file with id easy-file:1 found in files.xml");
    }

    @Test
    public void getAccessibilityForFileId_should_throw_when_no_accessibleToRights_found() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        assertThatThrownBy(() -> filesXml.getAccessibilityForFileId("easy-file:1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No accessibleToRights element found for file with id easy-file:1");
    }

    @Test
    public void addFile_should_add_a_file() throws Exception {
        String xml = "<files " + namespaceBindings + "></files>";
        FilesXml filesXml = new FilesXml(xml, testDir.resolve("files.xml"));
        filesXml.addFile("path/to/file", "ANONYMOUS");
        filesXml.write();

        Document document = XmlUtil.readXml(testDir.resolve("files.xml"));

        assertThat(document.getElementsByTagName("file").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagName("file").item(0).getAttributes().getNamedItem("filepath").getNodeValue())
            .isEqualTo("path/to/file");
    }

    @Test
    public void deleteFileElementForFilepath_should_delete_the_file() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to/file1\">"
            + "  <dct:identifier>easy-file:1</dct:identifier>"
            + " </file>"
            + " <file filepath=\"path/to-file:2\">"
            + "  <dct:identifier>easy-file:2</dct:identifier>"
            + " </file>"
            + "</files>";

        FilesXml filesXml = new FilesXml(xml, null);
        filesXml.deleteFileElementForFilepath("path/to/file1");
        assertThat(filesXml.getFileIds()).containsExactly("easy-file:2");
    }

    @Test
    public void deleteFileElementForFilePath_should_ignore_when_no_file_found() throws Exception {
        String xml = "<files " + namespaceBindings + ">"
            + " <file filepath=\"path/to-file:2\">"
            + "  <dct:identifier>easy-file:2</dct:identifier>"
            + " </file>"
            + "</files>";
        FilesXml filesXml = new FilesXml(xml, null);
        filesXml.deleteFileElementForFilepath("path/to/file1");
        assertThat(filesXml.getFileIds()).containsExactly("easy-file:2");
    }

}
