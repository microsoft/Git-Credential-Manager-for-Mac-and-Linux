package com.microsoft.alm.java.git_credential_helper.helpers;

import java.io.File;

/**
 * Equivalent to the .NET class with the same name.
 */
public class Environment
{
    public static String getCurrentDirectory()
    {
        // http://stackoverflow.com/a/16802976/
        final File currentDir = new File("");
        final String result = currentDir.getAbsolutePath();
        return result;
    }

    public static String getFolderPath(final SpecialFolder folder)
    {
        switch (folder)
        {
            case UserProfile:
                return System.getProperty("user.home");
            default:
                throw new NotImplementedException();
        }
    }

    public enum SpecialFolder
    {
        Desktop,
        Programs,
        MyDocuments,
        Personal,
        Favorites,
        Startup,
        Recent,
        SendTo,
        StartMenu,
        MyMusic,
        MyVideos,
        DesktopDirectory,
        MyComputer,
        NetworkShortcuts,
        Fonts,
        Templates,
        CommonStartMenu,
        CommonPrograms,
        CommonStartup,
        CommonDesktopDirectory,
        /**
         * The directory that serves as a common repository for application-specific data
         * for the current roaming user.
         * <p/>
         * A roaming user works on more than one computer on a network.
         * A roaming user's profile is kept on a server on the network and
         * is loaded onto a system when the user logs on.
         */
        ApplicationData,
        PrinterShortcuts,
        LocalApplicationData,
        InternetCache,
        Cookies,
        History,
        CommonApplicationData,
        Windows,
        System,
        ProgramFiles,
        MyPictures,
        /**
         * The user's profile folder.
         * Applications should not create files or folders at this level;
         * they should put their data under the locations referred to by {@link #ApplicationData}.
         */
        UserProfile,
        SystemX86,
        ProgramFilesX86,
        CommonProgramFiles,
        CommonProgramFilesX86,
        CommonTemplates,
        CommonDocuments,
        CommonAdminTools,
        AdminTools,
        CommonMusic,
        CommonPictures,
        CommonVideos,
        Resources,
        LocalizedResources,
        CommonOemLinks,
        CDBurning,
    }
}
