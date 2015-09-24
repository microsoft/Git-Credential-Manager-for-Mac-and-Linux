package com.microsoft.alm.java.git_credential_helper.cli;

/**
 * Type of authentication and identity authority expected.
 */
enum AuthorityType
{
    /**
     * Attempt to detect the authority automatically, fallback to {@link #Basic} if
     * unable to detect an authority.
     */
    Auto,
    /**
     * Basic username and password scheme
     */
    Basic,
    /**
     * Username and password scheme using Microsoft's Live system
     */
    MicrosoftAccount,
    /**
     * Azure Directory Authentication based, including support for ADFS
     */
    AzureDirectory,
    /**
     * GitHub authentication
     */
    GitHub,
    /**
     * Operating system / network integrated authentication layer.
     */
    Integrated,
}
