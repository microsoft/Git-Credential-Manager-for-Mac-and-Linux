package com.microsoft.alm.authentication;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public interface IGithubAuthentication extends IAuthentication
{
    boolean interactiveLogon(final URI targetUri, final AtomicReference<Credential> credentials);
    Future<Boolean> noninteractiveLogonWithCredentials(final URI targetUri, final String username, final String password, final String authenticationCode);
    Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials);
}
