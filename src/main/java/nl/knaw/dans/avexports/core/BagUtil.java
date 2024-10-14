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

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.domain.Metadata;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import nl.knaw.dans.bagit.exceptions.UnparsableVersionException;
import nl.knaw.dans.bagit.exceptions.UnsupportedAlgorithmException;
import nl.knaw.dans.bagit.reader.BagReader;
import nl.knaw.dans.bagit.writer.MetadataWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static nl.knaw.dans.avexports.core.XmlUtil.readXml;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class BagUtil {

    public static Bag getBag(Path bagDir) {
        try {
            return new BagReader().read(bagDir);
        }
        catch (IOException | UnparsableVersionException | MaliciousPathException | UnsupportedAlgorithmException | InvalidBagitFileFormatException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removePayloadManifestsForPath(Bag bag, String path) {
        Set<Manifest> manifestSet = bag.getPayLoadManifests();
        for (Manifest manifest : manifestSet) {
            manifest.getFileToChecksumMap().remove(bag.getRootDir().resolve(path));
        }
    }

    public static void updatePayloadManifestsForPath(Bag bag, String path) {
        Set<Manifest> manifestSet = bag.getPayLoadManifests();
        for (Manifest manifest : manifestSet) {
            manifest.getFileToChecksumMap().put(bag.getRootDir().resolve(path), new Hasher(bag.getRootDir().resolve(path), manifest.getAlgorithm().toString()).getChecksum());
        }
    }

    public static void updateTagManifestsForPath(Bag bag, String path) {
        Set<Manifest> manifestSet = bag.getTagManifests();
        for (Manifest manifest : manifestSet) {
            manifest.getFileToChecksumMap().put(bag.getRootDir().resolve(path), new Hasher(bag.getRootDir().resolve(path), manifest.getAlgorithm().toString()).getChecksum());
        }
    }

    public static Bag updateBagVersion(Path newBagDir, Path previousBagDir)
        throws IOException, UnparsableVersionException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException, ParserConfigurationException, SAXException {
        String now = ZonedDateTime
            .now(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Bag newBag = new BagReader().read(newBagDir);
        Metadata bagInfo = newBag.getMetadata();
        bagInfo.remove("Is-Version-Of");
        bagInfo.remove("Created");
        bagInfo.add("Is-Version-Of", "urn:uuid:" + previousBagDir.getParent().getFileName());
        bagInfo.add("Created", now);
        if (isEmpty(bagInfo.get("Base-URN")) || isEmpty(bagInfo.get("Base-DOI"))) {
            // once added for the second bag, we don't need to add for subsequent bags
            List<String> idTypes = Arrays.asList("DOI", "URN");
            NodeList idElements = ((Element) readXml(newBagDir.resolve("metadata/dataset.xml"))
                .getElementsByTagName("ddm:dcmiMetadata").item(0))
                .getElementsByTagName("dct:identifier");
            for (int i = 0; i < idElements.getLength(); i++) {
                Element id = (Element) idElements.item(i);
                String idType = id.getAttribute("xsi:type")
                    .replace("id-type:", "");
                if (idTypes.contains(idType)) {
                    bagInfo.remove("Base-" + idType);
                    bagInfo.add("Base-" + idType, id.getTextContent());
                }
            }
        }
        MetadataWriter.writeBagMetadata(bagInfo, newBag.getVersion(), newBag.getRootDir(), StandardCharsets.UTF_8);
        return newBag;
    }
}
