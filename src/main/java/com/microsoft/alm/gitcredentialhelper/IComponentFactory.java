package com.microsoft.alm.gitcredentialhelper;

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
