package com.microsoft.alm.java.git_credential_helper.authentication;

import java.net.URI;
import java.util.concurrent.Future;

public interface IVsoMsaAuthentication extends IAuthentication
{
    boolean interactiveLogon(final URI targetUri, boolean requestCompactToken);
    Future<Boolean> refreshCredentials(final URI targetUri, final boolean requireCompactToken);
    Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials);
}
