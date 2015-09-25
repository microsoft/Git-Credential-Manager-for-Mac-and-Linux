package com.microsoft.alm.java.git_credential_helper.cli;

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
