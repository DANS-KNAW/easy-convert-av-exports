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

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class XmlNamespacesContext implements XmlNamespaces, NamespaceContext {
    private static final XmlNamespacesContext INSTANCE = new XmlNamespacesContext();

    public static XmlNamespacesContext getInstance() {
        return INSTANCE;
    }

    private final Map<String, String> prefixToNamespaceMap = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("xml", NAMESPACE_XML);
        put("dc", NAMESPACE_DC);
        put("dct", NAMESPACE_DCTERMS);
        put("dcx-dai", NAMESPACE_DCX_DAI);
        put("ddm", NAMESPACE_DDM);
        put("dcterms", NAMESPACE_DCTERMS);
        put("xsi", NAMESPACE_XSI);
        put("id-type", NAMESPACE_ID_TYPE);
        put("dcx-gml", NAMESPACE_DCX_GML);
        put("files", NAMESPACE_FILES_XML);
        put("gml", NAMESPACE_OPEN_GIS);
    }});

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixToNamespaceMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<String, String> entry : prefixToNamespaceMap.entrySet()) {
            if (entry.getValue().equals(namespaceURI)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String s) {
        List<String> prefixes = new LinkedList<>();
        for (Map.Entry<String, String> entry : prefixToNamespaceMap.entrySet()) {
            if (entry.getValue().equals(s)) {
                prefixes.add(entry.getKey());
            }
        }
        return prefixes.iterator();
    }
}
