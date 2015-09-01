package com.microsoft.alm.java.git_credential_helper.cli;

import com.microsoft.alm.java.git_credential_helper.authentication.Credential;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.net.URI;

final class OperationArguments
{
    OperationArguments(final BufferedReader stdin)
    {
        throw new NotImplementedException();
    }

    public final String Protocol;
    public final String Host;
    public final String Path;
    public final URI TargetUri;

    public String getUserName()
    {
        throw new NotImplementedException();
    }

    public String getPassword()
    {
        throw new NotImplementedException();
    }

    public AuthorityType getAuthority()
    {
        throw new NotImplementedException();
    }

    public void setAuthority(final AuthorityType value)
    {
        throw new NotImplementedException();
    }

    public Interactivity getInteractivity()
    {
        throw new NotImplementedException();
    }

    public void setInteractivity(final Interactivity value)
    {
        throw new NotImplementedException();
    }

    public boolean getValidateCredentials()
    {
        throw new NotImplementedException();
    }

    public void setValidateCredentials(final boolean value)
    {
        throw new NotImplementedException();
    }

    public boolean getWriteLog()
    {
        throw new NotImplementedException();
    }

    public void setWriteLog(final boolean value)
    {
        throw new NotImplementedException();
    }

    public void setCredentials(final Credential credentials)
    {
        throw new NotImplementedException();
    }

    @Override
    public String toString()
    {
        throw new NotImplementedException();
    }
}
