package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Func;
import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;
import com.microsoft.alm.java.git_credential_helper.helpers.ObjectExtensions;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;

import java.io.File;
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
        if (!StringHelper.isNullOrWhiteSpace(name))
        {
            final String pathext = ObjectExtensions.coalesce(System.getenv("PATHEXT"), StringHelper.Empty);
            final String envpath = ObjectExtensions.coalesce(System.getenv("PATH"), StringHelper.Empty);

            final String pathSeparator = System.getProperty("path.separator");
            final Func<String, Boolean> existenceChecker = new Func<String, Boolean>()
            {
                @Override public Boolean call(final String s)
                {
                    return new File(s).exists();
                }
            };
            if (app(name, path, pathext, envpath, pathSeparator, existenceChecker))
            {
                return true;
            }
        }
        path.set(null);
        return false;
    }

    static boolean app(
            final String name,
            final AtomicReference<String> path,
            final String pathext,
            final String envpath,
            final String pathSeparator,
            final Func<String, Boolean> existenceChecker)
    {
        final String[] exts = pathext.split(pathSeparator);
        final String[] paths = envpath.split(pathSeparator);

        for (int i = 0; i < paths.length; i++)
        {
            if (StringHelper.isNullOrWhiteSpace(paths[i]))
                continue;

            for (int j = 0; j < exts.length; j++)
            {
                // we need to consider the case without an extension,
                // so skip the null or empty check that was in the C# version

                final String value = String.format("%1$s/%2$s%3$s", paths[i], name, exts[j]);
                final Boolean result = existenceChecker.call(value);
                if (result != null && (boolean)result)
                {
                    path.set(value);
                    return true;
                }
            }
        }
        return false;
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
