// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

public class NotImplementedException extends RuntimeException
{
    private final int workItemNumber;

    public NotImplementedException()
    {
        this(-1);
    }

    public NotImplementedException(final int workItemNumber)
    {
        this.workItemNumber = workItemNumber;
    }

    public int getWorkItemNumber()
    {
        return workItemNumber;
    }
}
