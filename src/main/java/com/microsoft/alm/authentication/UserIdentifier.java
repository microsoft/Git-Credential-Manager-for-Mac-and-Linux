// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.StringHelper;

/**
 * Contains identifier for a user.
 */
public final class UserIdentifier {
    private static final String ANY_USER_ID = "AnyUser";
    /**
     * A static instance of {@link UserIdentifier} to represent any user.
     */
    public static final UserIdentifier ANY_USER = new UserIdentifier(ANY_USER_ID, UserIdentifierType.UNIQUE_ID);

    private final String id;
    private final UserIdentifierType type;

    public UserIdentifier(final String id, final UserIdentifierType type) {
        if (StringHelper.isNullOrWhiteSpace(id)) {
            throw new IllegalArgumentException("id is null or empty");
        }
        this.id = id;
        this.type = type;
    }

    /**
     * @return the type of the {@link UserIdentifier}.
     */
    public UserIdentifierType getType() {
        return this.type;
    }

    /**
     * @return id of the {@link UserIdentifier}.
     */
    public String getId() {
        return this.id;
    }

    boolean isAnyUser() {
        return this.type == ANY_USER.type && this.id.equals(ANY_USER.id);
    }

    String getUniqueId() {
        return (!this.isAnyUser() && this.type == UserIdentifierType.UNIQUE_ID) ? this.id : null;
    }

    String getDisplayableId() {
        return (!this.isAnyUser() && (this.type == UserIdentifierType.OPTIONAL_DISPLAYABLE_ID || this.type == UserIdentifierType.REQUIRED_DISPLAYABLE_ID)) ? this.id : null;
    }
}
