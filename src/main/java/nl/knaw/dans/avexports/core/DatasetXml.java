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

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents the dataset.xml file in an AV export. It provides methods to perform the necessary lookups.
 */
@Slf4j
public class DatasetXml {
    private final Document document;

    public DatasetXml(Path path) throws ParserConfigurationException, IOException, SAXException {
        this.document = XmlUtil.readXml(path);
    }

    // For testing purposes
    DatasetXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        this.document = XmlUtil.readXmlFromString(xml);
    }

    public String getDatasetId() throws XPathExpressionException {
        NodeList attrs = XmlUtil.getNodeListByXPath(document, "/ddm:DDM/ddm:dcmiMetadata/dct:identifier[@xsi:type='id-type:EASY2']");
        if (attrs.getLength() == 0) {
            throw new IllegalStateException("No datasetId found in the dataset.xml");
        } else if (attrs.getLength() > 1) {
            throw new IllegalStateException("Multiple datasetIds found in the dataset.xml");
        }
        return attrs.item(0).getTextContent();
    }

}
