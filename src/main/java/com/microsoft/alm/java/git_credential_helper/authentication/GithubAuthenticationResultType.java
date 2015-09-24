package com.microsoft.alm.java.git_credential_helper.authentication;

public enum GithubAuthenticationResultType
{
    Success,
    Failure,
    TwoFactorApp,
    TwoFactorSms,
}
