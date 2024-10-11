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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesXml {
    private final Document document;
    private final Path path;

    public FilesXml(Path path) throws ParserConfigurationException, IOException, SAXException {
        this.path = path;
        this.document = XmlUtil.readXml(path);
    }

    public Path getFilepathForFileId(String id) throws XPathExpressionException {
        return Paths.get(getElementById(id).getAttributes().getNamedItem("filepath").getNodeValue());
    }

    public void setFilepathForFileId(String fieldId, Path path) {
        try {
            getElementById(fieldId).getAttributes().getNamedItem("filepath").setNodeValue(path.toString());
        }
        catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private Node getElementById(String id) throws XPathExpressionException {
        return XmlUtil.getNodesByXPath(document, "/files/file/dct:identifier[text()='" + id + "']/..");
    }

    public void write() {
        XmlUtil.writeXmlTo(document, path);
    }

}
