// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

// TODO: [DebuggerDisplay("{Type}")]
public class GithubAuthenticationResult
{
    public GithubAuthenticationResult(final GithubAuthenticationResultType type)
    {
        this.Type = type;
        this.token = null;
    }

    public GithubAuthenticationResult(final GithubAuthenticationResultType type, final Token token)
    {
        this.Type = type;
        this.token = token;
    }

    public final GithubAuthenticationResultType Type;
    Token token;
    public Token getToken()
    {
        return token;
    }

    public static boolean operatorBoolean(final GithubAuthenticationResult result)
    {
        return result.Type == GithubAuthenticationResultType.Success;
    }

    public static GithubAuthenticationResultType operatorGithubAuthenticationResultType(final GithubAuthenticationResult result)
    {
        return result.Type;
    }

    public static GithubAuthenticationResult operatorGithubAuthenticationResult(final GithubAuthenticationResultType type)
    {
        return new GithubAuthenticationResult(type);
    }

}
