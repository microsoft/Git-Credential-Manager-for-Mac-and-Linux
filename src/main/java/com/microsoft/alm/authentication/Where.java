package com.microsoft.alm.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Environment;
import com.microsoft.alm.java.git_credential_helper.helpers.Func;
import com.microsoft.alm.java.git_credential_helper.helpers.IteratorExtensions;
import com.microsoft.alm.java.git_credential_helper.helpers.ObjectExtensions;
import com.microsoft.alm.java.git_credential_helper.helpers.Path;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Where
{
    private static final Pattern gitdirPattern = Pattern.compile("gitdir\\s*:\\s([^\\r\\n]+)", Pattern.CASE_INSENSITIVE);

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
        final String GlobalConfigFileName = ".gitconfig";

        path.set(null);

        String globalPath = Path.combine(Environment.getFolderPath(Environment.SpecialFolder.UserProfile), GlobalConfigFileName);

        if (Path.fileExists(globalPath))
        {
            path.set(globalPath);
        }

        return path.get() != null;
    }

    /**
     * Gets the path to the Git local configuration file based on the startingDirectory.
     *
     * @param startingDirectory A directory of the repository where the configuration file is contained.
     * @param path Path to the Git local configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitLocalConfig(final String startingDirectory, final AtomicReference<String> path) throws IOException
    {
        final String GitOdbFolderName = ".git";
        final String LocalConfigFileName = "config";

        path.set(null);

        if (!StringHelper.isNullOrWhiteSpace(startingDirectory))
        {
            File dir = new File(startingDirectory);
            if (dir.exists())

            {
                // the hasOdb Func was simplified to an exists() check
                File result = null;
                while (dir != null && dir.exists() && dir.getParentFile() != null && dir.getParentFile().exists())
                {
                    result = new File(dir, GitOdbFolderName);
                    if (result.exists())
                        break;

                    dir = dir.getParentFile();
                }

                if (result != null && result.exists())
                {
                    if (result.isDirectory())
                    {
                        final String localPath = Path.combine(result.getAbsolutePath(), LocalConfigFileName);
                        if (Path.fileExists(localPath))
                        {
                            path.set(localPath);
                        }
                    }
                    else
                    {
                        // parse the file like gitdir: ../.git/modules/libgit2sharp
                        String content = null;

                        // shortcut the opening streams & readers just to read the whole file as a string
                        content = FileUtils.readFileToString(result);

                        final Matcher match;
                        if ((match = gitdirPattern.matcher(content)).matches()
                            && match.groupCount() > 1)
                        {
                            content = match.group(1);
                            // don't replace / with \\

                            String localPath = null;

                            if (Path.isAbsolute(content))
                            {
                                localPath = content;
                            }
                            else
                            {
                                localPath = Path.getDirectoryName(result.getAbsolutePath());
                                localPath = Path.combine(localPath, content);
                            }

                            if (Path.directoryExists(localPath))
                            {
                                localPath = Path.combine(localPath, LocalConfigFileName);
                                if (Path.fileExists(localPath))
                                {
                                    path.set(localPath);
                                }
                            }
                        }
                    }
                }
            }
        }

        return path.get() != null;
    }

    /**
     * Gets the path to the Git local configuration file based on the current working directory.
     *
     * @param path Path to the Git local configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitLocalConfig(final AtomicReference<String> path) throws IOException
    {
        return gitLocalConfig(Environment.getCurrentDirectory(), path);
    }

    /**
     * Gets the path to the Git system configuration file.
     *
     * @param path Path to the Git system configuration.
     * @return True if succeeds; false otherwise.
     */
    public static boolean gitSystemConfig(final AtomicReference<String> path)
    {
        final String SystemConfigFileName = "gitconfig";

        // find Git on the local disk - the system config is stored relative to it
        if (app("git", path))
        {
            File gitInfo = new File(path.get());
            File dir = gitInfo.getParentFile();
            if (dir.getParentFile() != null)
            {
                dir = dir.getParentFile();
            }

            File file = IteratorExtensions.firstOrDefault(FileUtils.iterateFiles(dir, new NameFileFilter(SystemConfigFileName), TrueFileFilter.INSTANCE));
            if (file != null && file.exists())
            {
                path.set(file.getAbsolutePath());
                return true;
            }
        }

        path.set(null);
        return false;
    }
}
