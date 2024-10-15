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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the files.xml file in an AV export. It provides methods to perform the necessary lookups and changes and to write the changes back to the file.
 */
@Slf4j
public class FilesXml {
    private final Document document;
    private final Path path;

    public FilesXml(Path path) throws ParserConfigurationException, IOException, SAXException {
        this.path = path;
        this.document = XmlUtil.readXml(path);
    }

    // For testing purposes
    FilesXml(String xml, Path path) throws ParserConfigurationException, IOException, SAXException {
        this.document = XmlUtil.readXmlFromString(xml);
        this.path = path;
    }

    public List<String> getFileIds() throws XPathExpressionException {
        NodeList identifierList = XmlUtil.getNodeListByXPath(document, "//dct:identifier/text()");
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < identifierList.getLength(); i++) {
            ids.add(identifierList.item(i).getNodeValue());
        }
        return ids;
    }

    public void removeFile(String id) throws XPathExpressionException {
        Node fileNode = getElementById(id);
        if (fileNode == null) {
            throw new IllegalArgumentException("No file with id " + id + " found in files.xml");
        }
        fileNode.getParentNode().removeChild(fileNode);
    }

    public String getFilepathForFileId(String id) throws XPathExpressionException {
        Node fileNode = getElementById(id);
        if (fileNode == null) {
            throw new IllegalArgumentException("No file with id " + id + " found in files.xml");
        }
        return fileNode.getAttributes().getNamedItem("filepath").getNodeValue();
    }

    public void setFilepathForFileId(String id, String path) {
        try {
            Node fileNode = getElementById(id);
            if (fileNode == null) {
                throw new IllegalArgumentException("No file with id " + id + " found in files.xml");
            }
            fileNode.getAttributes().getNamedItem("filepath").setNodeValue(path);
        }
        catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private Node getElementById(String id) throws XPathExpressionException {
        // Java's XPath implementation does not seem to support default namespaces, so we have to access the DOM directly to get at the file element.
        NodeList nodeList = document.getElementsByTagName("file");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (XmlUtil.getNodeByXPath(node, "./dct:identifier[text() = '" + id + "']") != null) {
                return node;
            }
        }
        return null;
    }

    public void write() {
        XmlUtil.writeXmlTo(document, path);
    }

}
