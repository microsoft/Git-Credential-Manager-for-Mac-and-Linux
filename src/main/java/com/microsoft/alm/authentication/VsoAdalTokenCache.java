// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.NotImplementedException;

class VsoAdalTokenCache // TODO: 449520: extends TokenCache
{
    private static final String AdalCachePath = "Microsoft\\VSCommon\\VSAccountManagement";
    private static final String AdalCacheFile = "AdalCache.cache";

    /**
     * Default constructor.
     */
    public VsoAdalTokenCache()
    {
        throw new NotImplementedException(449520);
    }

    /**
     * Constructor receiving state of the cache.
     *
     * @param state Current state of the cache as a blob.
     */
    public VsoAdalTokenCache(final byte[] state)
    {
        throw new NotImplementedException(449520);
    }

    private final String _cacheFilePath;

    private final Object lock = new Object();

    // TODO: 449520: arguments should be TokenCacheNotificationArgs
    private void afterAccessNotification(final Object args)
    {
        throw new NotImplementedException(449520);
    }


    private void beforeAccessNotification(final Object args)
    {
        throw new NotImplementedException(449520);
    }
}
