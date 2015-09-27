// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 */
public interface Func<T, R>
{
    /**
     * Calls the function with the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R call(T t);
}
