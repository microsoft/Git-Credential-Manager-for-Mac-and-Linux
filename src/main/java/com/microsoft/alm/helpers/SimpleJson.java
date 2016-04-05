// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A very simple JSON [de-]serializer that only handles a dictionary of scalars.
 */
public class SimpleJson {

    public static Map<String, Object> parse(final String input) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();

        return result;
    }

    public static String format(final Map<String, Object> input) {
        return null;
    }

}
