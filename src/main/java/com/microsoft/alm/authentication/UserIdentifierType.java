// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

/**
 * Indicates the type of {@link UserIdentifier}
 */
public enum UserIdentifierType {
    /**
     * When a {@link UserIdentifier} of this type is passed in a token acquisition operation,
     * the operation is guaranteed to return a token issued for the user with corresponding
     * {@link UserIdentifier#getUniqueId()} or fail.
     */
    UNIQUE_ID,

    /**
     * When a {@link UserIdentifier} of this type is passed in a token acquisition operation,
     * the operation restricts cache matches to the value provided and injects it as a hint in the
     * authentication experience. However the end user could overwrite that value, resulting in a token
     * issued to a different account than the one specified in the {@link UserIdentifier} in input.
     */
    OPTIONAL_DISPLAYABLE_ID,

    /**
     * When a {@link UserIdentifier} of this type is passed in a token acquisition operation,
     * the operation is guaranteed to return a token issued for the user with corresponding
     * {@link UserIdentifier#getDisplayableId()} (UPN or email) or fail
     */
    REQUIRED_DISPLAYABLE_ID,
    ;
}
