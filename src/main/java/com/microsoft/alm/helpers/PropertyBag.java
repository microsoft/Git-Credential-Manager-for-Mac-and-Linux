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
        final int result;
        if (containsKey(key)) {
            final Object candidateResult = get(key);
            if (candidateResult instanceof Double) {
                final Double resultAsDouble = (Double) candidateResult;
                result = (int) Math.round(resultAsDouble);
            }
            else if (candidateResult instanceof String) {
                final String resultAsString = (String) candidateResult;
                result = Integer.parseInt(resultAsString, 10);
            }
            else {
                result = defaultValue;
            }
        }
        else {
            result = defaultValue;
        }

        return result;
    }

    public String readOptionalString(final String key, final String defaultValue) {
        final String result;
        if (containsKey(key)) {
            result = (String) get(key);
        }
        else {
            result = defaultValue;
        }
        return result;
    }
}
