package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.util.concurrent.atomic.AtomicReference;

public class Where
{
    /**
     * Finds the "best" path to an app of a given name.
     *
     * @param name The name of the application, without extension, to find.
     * @param path Path to the first match file which the operating system considers
     *             executable.
     * @return True if succeeds; false otherwise.
     */
    public static boolean app(final String name, final AtomicReference<String> path)
    {
        throw new NotImplementedException();
    }

    /**
     * Gets the path to the Git global configuration file.
     *
     * @param path Path to the Git global configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitGlobalConfig(final AtomicReference<String> path)
    {
        throw new NotImplementedException();
    }

    /**
     * Gets the path to the Git local configuration file based on the startingDirectory.
     *
     * @param startingDirectory A directory of the repository where the configuration file is contained.
     * @param path Path to the Git local configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitLocalConfig(final String startingDirectory, final AtomicReference<String> path)
    {
        throw new NotImplementedException();
    }
    /**
     * Gets the path to the Git local configuration file based on the current working directory.
     *
     * @param path Path to the Git local configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitLocalConfig(final AtomicReference<String> path)
    {
        throw new NotImplementedException();
    }

    /**
     * Gets the path to the Git system configuration file.
     *
     * @param path Path to the Git system configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitSystemConfig(final AtomicReference<String> path)
    {
        throw new NotImplementedException();
    }
}
