// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper
{
    // Adapted from http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
    public static String getText(final Node node) {
        final StringBuilder result = new StringBuilder();
        if (! node.hasChildNodes()) return "";

        final NodeList list = node.getChildNodes();
        for (int i=0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE) {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() == Node.CDATA_SECTION_NODE) {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                // Recurse into the subtree for text
                // (and ignore comments)
                result.append(getText(subnode));
            }
        }

        return result.toString();
    }
}
