// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

/**
 * Represents a method that accepts one argument.
 *
 * @param <T> the type of the input to the method
 */
public interface Action<T>
{
    /**
     * Calls the method with the given argument
     *
     * @param t the method argument
     */
    void call(final T t);
}
