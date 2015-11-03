// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

public class NotImplementedException extends RuntimeException
{
    private final int workItemNumber;
    private final String details;

    public NotImplementedException(final int workItemNumber)
    {
        this(workItemNumber, null);
    }

    public NotImplementedException(final int workItemNumber, final String details)
    {
        super("This feature is not yet implemented, but is tracked by work item #" + workItemNumber + ((details != null) ? ". " + details : "."));
        this.workItemNumber = workItemNumber;
        this.details = details;
    }

    public int getWorkItemNumber()
    {
        return workItemNumber;
    }

    public String getDetails()
    {
        return details;
    }
}
