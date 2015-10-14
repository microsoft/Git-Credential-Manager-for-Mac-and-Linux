// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.util.LinkedHashMap;

public class QueryString extends LinkedHashMap<String, String>
{
    @Override
    public String toString()
    {
        return UriHelper.serializeParameters(this);
    }
}
