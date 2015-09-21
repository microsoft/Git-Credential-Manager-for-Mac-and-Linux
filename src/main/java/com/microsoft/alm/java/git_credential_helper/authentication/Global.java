package com.microsoft.alm.java.git_credential_helper.authentication;

final class Global
{
    public static final int PasswordMaxLength = 2047;
    public static final int UsernameMaxLength = 511;

    private static String userAgent = null;

    /**
     * Creates the correct user-agent string for HTTP calls.
     *
     * @return The `user-agent` string for "git-tools".
     * Example from Windows version:
     * git-credential-manager (Microsoft Windows NT 6.2.9200.0; Win32NT; x64) CLR/4.0.30319 git-tools/1.0.0
     * Example from Java version:
     * git-credential-manager (Windows 8.1; 6.3; amd64) Java HotSpot(TM) 64-Bit Server VM/1.8.0_51-b16 git-tools/1.0.0-SNAPSHOT
     */
    public static String getUserAgent()
    {
        if (userAgent == null)
        {
            // http://stackoverflow.com/a/6773868/
            final String version = Global.class.getPackage().getImplementationVersion();
            userAgent = String.format("git-credential-manager (%1$s; %2$s; %3$s) %4$s/%5$s git-tools/%6$s",
                    System.getProperty("os.name"), // "Windows Server 2012 R2", "Mac OS X", "Linux"
                    System.getProperty("os.version"), // "6.3", "10.10.5", "3.19.0-28-generic"
                    System.getProperty("os.arch"), // "amd64", "x86_64", "amd64"
                    System.getProperty("java.vm.name"), // "Java HotSpot(TM) 64-Bit Server VM", "OpenJDK 64-Bit Server VM"
                    System.getProperty("java.runtime.version"), // "1.8.0_60-b27", "1.7.0_71-b14", "1.7.0_79-b14"
                    version);
        }
        return userAgent;
    }
}
