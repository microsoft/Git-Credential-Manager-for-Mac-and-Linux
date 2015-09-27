// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialhelper;

import com.microsoft.alm.authentication.Credential;
import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.ObjectExtensions;
import com.microsoft.alm.helpers.StringHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

final class OperationArguments
{
    OperationArguments(final BufferedReader stdin) throws IOException, URISyntaxException
    {
        Debug.Assert(stdin != null, "The stdin parameter is null");

        this.Authority = AuthorityType.Basic;
        this.Interactivity = com.microsoft.alm.gitcredentialhelper.Interactivity.Auto;
        this.ValidateCredentials = true;
        this.WriteLog = false;

        String protocol = null;
        String host = null;
        String path = null;
        String line;
        while (!StringHelper.isNullOrWhiteSpace((line = stdin.readLine())))
        {
            String[] pair = line.split("=", 2);

            if (pair.length == 2)
            {
                // Java doesn't support a switch on a String!
                if ("protocol".equals(pair[0]))
                {
                    protocol = pair[1];
                }
                else if ("host".equals(pair[0]))
                {
                    host = pair[1];
                }
                else if ("path".equals(pair[0]))
                {
                    path = pair[1];
                }
                else if ("username".equals(pair[0]))
                {
                    userName = pair[1];
                }
                else if ("password".equals(pair[0]))
                {
                    password = pair[1];
                }
            }
        }
        Protocol = protocol;
        Host = host;
        Path = path;

        if (this.Protocol != null && this.Host != null)
        {
            this.TargetUri = new URI(String.format("%1$s://%2$s/", this.Protocol, this.Host));
        }
        else
        {
            this.TargetUri = null;
        }
    }

    public final String Protocol;
    public final String Host;
    public final String Path;
    public final URI TargetUri;

    private String userName;
    public String getUserName()
    {
        return userName;
    }

    private String password;
    public String getPassword()
    {
        return password;
    }

    public AuthorityType Authority;

    public Interactivity Interactivity;

    public boolean ValidateCredentials;

    public boolean WriteLog;

    public void setCredentials(final Credential credentials)
    {
        this.userName = credentials.Username;
        this.password = credentials.Password;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();

        builder.append("protocol=")
                .append(ObjectExtensions.coalesce(this.Protocol, StringHelper.Empty))
                .append("\n");
        builder.append("host=")
                .append(ObjectExtensions.coalesce(this.Host, StringHelper.Empty))
                .append("\n");
        builder.append("path=")
                .append(ObjectExtensions.coalesce(this.Path, StringHelper.Empty))
                .append("\n");
        // only write out username if we know it
        if (this.userName != null)
        {
            builder.append("username=")
                    .append(this.userName)
                    .append("\n");
        }
        // only write out password if we know it
        if (this.password != null)
        {
            builder.append("password=")
                    .append(this.password)
                    .append("\n");
        }

        return builder.toString();
    }
}
