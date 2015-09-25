// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

public enum GithubAuthenticationResultType
{
    Success,
    Failure,
    TwoFactorApp,
    TwoFactorSms,
}
