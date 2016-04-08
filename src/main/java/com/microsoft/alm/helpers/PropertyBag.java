// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.util.LinkedHashMap;

public class PropertyBag extends LinkedHashMap<String, Object> {

    public static PropertyBag fromJson(final String input) {
        final PropertyBag result = new PropertyBag();
        SimpleJson.parse(input, result);
        return result;
    }

    public int readOptionalInteger(final String key, final int defaultValue) {
        return SimpleJson.readOptionalInteger(this, key, defaultValue);
    }
}
