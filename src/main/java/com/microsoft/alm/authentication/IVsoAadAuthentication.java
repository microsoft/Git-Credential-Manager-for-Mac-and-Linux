package com.microsoft.alm.authentication;

import java.net.URI;
import java.util.concurrent.Future;

public interface IVsoAadAuthentication extends IAuthentication
{
    boolean interactiveLogon(final URI targetUri, final boolean requestCompactToken);
    Future<Boolean> noninteractiveLogonWithCredentials(final URI targetUri, final Credential credentials, final boolean requestCompactToken);
    Future<Boolean> noninteractiveLogon(final URI targetUri, final boolean requestCompactToken);
    Future<Boolean> refreshCredentials(final URI targetUri, final boolean requireCompactToken);
    Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials);
}
