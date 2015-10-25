// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
            case ApplicationData:
                return System.getenv("APPDATA");
            case LocalApplicationData:
                return System.getenv("LOCALAPPDATA");
            case UserProfile:
                return System.getProperty("user.home");
            default:
                throw new IllegalArgumentException("Very few SpecialFolder flags are supported.");
        }
    }

    public static String getMachineName()
    {
        String machineName = null;
        if (machineName == null)
        {
            machineName = System.getenv("COMPUTERNAME");
        }
        if (machineName == null)
        {
            machineName = System.getenv("HOSTNAME");
        }
        if (machineName == null)
        {
            try
            {
                final InetAddress address = InetAddress.getLocalHost();
                machineName = address.getHostName();
            }
            catch (final UnknownHostException e)
            {
                machineName = "unknown";
            }
        }
        return machineName;
    }

    public static final String NewLine = System.getProperty("line.separator");

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
         *
         * A roaming user works on more than one computer on a network.
         * A roaming user's profile is kept on a server on the network and
         * is loaded onto a system when the user logs on.
         */
        ApplicationData,
        PrinterShortcuts,
        /**
         * The directory that serves as a common repository for application-specific data
         * that is used by the current, non-roaming user.
         */
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
