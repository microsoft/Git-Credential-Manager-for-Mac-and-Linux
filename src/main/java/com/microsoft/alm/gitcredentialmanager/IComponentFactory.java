// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.authentication.Configuration;
import com.microsoft.alm.authentication.IAuthentication;
import com.microsoft.alm.authentication.ISecureStore;

import java.io.IOException;

public interface IComponentFactory
{
    IAuthentication createAuthentication(final OperationArguments operationArguments, final ISecureStore secureStore);
    Configuration createConfiguration() throws IOException;
    ISecureStore createSecureStore();
}
