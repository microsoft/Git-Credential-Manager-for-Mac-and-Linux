package com.microsoft.alm.java.git_credential_helper.authentication;

import java.net.URI;

public interface IVsoMsaAuthentication
{
    boolean interactiveLogon(final URI targetUri, boolean requestCompactToken);
}
