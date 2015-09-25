package com.microsoft.alm.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;
import com.microsoft.alm.java.git_credential_helper.helpers.Trace;

import java.net.URI;

public abstract class Secret
{
    public static String uriToName(final URI targetUri, final String namespace)
    {
        final String TokenNameBaseFormat = "%1$s:%2$s://%3$s";
        final String TokenNamePortFormat = TokenNameBaseFormat + ":%4$s";

        Debug.Assert(targetUri != null, "The targetUri parameter is null");

        Trace.writeLine("Secret::uriToName");

        String targetName = null;
        // trim any trailing slashes and/or whitespace for compat with git-credential-winstore
        final String trimmedHostUrl = StringHelper.trimEnd(StringHelper.trimEnd(targetUri.getHost(), '/', '\\'));


        if (targetUri.getPort() == -1 /* isDefaultPort */)
        {
            targetName = String.format(TokenNameBaseFormat, namespace, targetUri.getScheme(), trimmedHostUrl);
        }
        else
        {
            targetName = String.format(TokenNamePortFormat, namespace, targetUri.getScheme(), trimmedHostUrl, targetUri.getPort());
        }

        Trace.writeLine("   target name = " + targetName);

        return targetName;
    }

    public interface IUriNameConversion
    {
        String convert(final URI targetUri, final String namespace);
    }

    public static IUriNameConversion DefaultUriNameConversion = new IUriNameConversion()
    {
        @Override
        public String convert(final URI targetUri, final String namespace)
        {
            return Secret.uriToName(targetUri, namespace);
        }
    };
}
