# Installation Instructions
This document explains how to obtain the Git Credential Manager for Mac and Linux (GCM4ML) and how to configure Git to use the GCM4ML as its credential helper.


## System Requirements

Great care was taken to avoid using any features of Java that would impact compatibility with Java 6.  If you find a compatibility issue, please report it and provide as many details about your platform as necessary to reproduce the problem.

1. Mac OS X version 10.9.5 and up OR a recent GNU/Linux distribution.
2. Java 6 and up.
3. Git version 1.9 and up.


## How to install
On Mac OS X, installing via Homebrew is highly recommended.
On Linux, it is recommended to use Linuxbrew or an RPM-based package manager if you can.

If you can't use any of the package managers, you can also download [git-credential-manager-2.0.4.jar](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/releases/download/git-credential-manager-2.0.4/git-credential-manager-2.0.4.jar) somewhere safe and stable, such as `~/git-credential-manager/`, and then follow the instructions for automatic or manual configuration.


### Installing on Mac using Homebrew or on Linux using Linuxbrew (recommended)


1. Update the Homebrew/Linuxbrew formulae to make sure you have the latest versions:
    ```
    brew update
    ```
2. Install the GCM4ML formula:

    ```
    brew install git-credential-manager
    ```
3. Run the GCM4ML in `install` mode, which will check its requirements and then update the "global" Git configuration file (the one in your home folder):

    ```
    git-credential-manager install
    ```

    
### Installing on Linux using RPM (recommended)

1. Download [git-credential-manager-2.0.4-1.noarch.rpm](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/releases/download/git-credential-manager-2.0.4/git-credential-manager-2.0.4-1.noarch.rpm) and copy the file somewhere locally.
2. Download the [PGP key used to sign the RPM](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/blob/master/RPM-GPG-KEY.txt).
3. Import the signing key into RPM's database:

    ```
    sudo rpm --import RPM-GPG-KEY.txt
    ```
4. Verify the GCM4ML RPM:

    ```
    rpm --checksig --verbose git-credential-manager-2.0.4-1.noarch.rpm
    ```
    ...you should see a line (among those there) that is equal to the following:
    
    ```
    V4 RSA/SHA256 Signature, key ID ba34dbc2: OK
    ```
5. Install the RPM:

    ```
    sudo rpm --install git-credential-manager-2.0.4-1.noarch.rpm
    ```
6. Run the GCM4ML in `install` mode, which will check its requirements and then update the "global" Git configuration file (the one in your home folder):

    ```
    git-credential-manager install
    ```


### Installing on Mac or Linux without a package manager
Download [git-credential-manager-2.0.4.jar](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/releases/download/git-credential-manager-2.0.4/git-credential-manager-2.0.4.jar) somewhere safe and stable, such as `~/git-credential-manager/`

#### Automatic configuration (recommended)
1. Run the GCM4ML in `install` mode, which will check its requirements and then update the "global" Git configuration file (the one in your home folder):

    ```
    java -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar install
    ```

#### Manual configuration
1. Configure the `credential.helper` setting to launch Java with the absolute path to the JAR (make sure you surround the whole value with 'single quotes'):

    ```
    git config --global credential.helper '!java -Ddebug=false -Djava.net.useSystemProxies=true -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar'
    ```


## How to enable (or disable) debug mode

Debug mode will turn on tracing and assertions, producing a lot of output to `stderr`.  Only turn this on temporarily, when trying to isolate a defect.


### Automatic configuration (recommended)
1. Run the GCM4ML in `install` mode with the `debug` property set to `true` (or `false` to disable):

    ```
    java -Ddebug=true -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar install
    ```

### Manual configuration
1. Retrieve the value of the `credential.helper` configuration:

    ```
    git config --global --get credential.helper git-credential-manager
    ```
    ...it should look like this:

    ```
    !java -Ddebug=false -Djava.net.useSystemProxies=true -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar
    ```
2. Set a new value for the `credential.helper` configuration (essentially repeating the _manual configuration_ installation step, being careful with quoting and spaces), changing the value of the `debug` property to `true` (or `false` to disable).


## How to configure the proxy server
If your network does not allow a direct connection to remote hosts, you can configure the GCM4ML to perform requests through a web proxy.

### Automatic configuration (recommended)
If you are running Gnome 2.x or greater, you can configure the proxy settings using the GUI and the GCM4ML will use those settings thanks to a JVM feature that's activated by setting the `java.net.useSystemProxies` system property to `true` (this is now done automatically when running the GCM4ML in `install` mode).

### Manual configuration

If it's not possible to use the automatic proxy server configuration, you must set the appropriate [networking properties](https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html). Aside from SOCKS proxy servers, which can have their credentials specified through specific properties, authenticated proxy servers are currently not supported.

1. Retrieve the value of the `credential.helper` configuration:

    ```
    git config --global --get credential.helper git-credential-manager
    ```
    ...it should look like this:

    ```
    !java -Ddebug=false -Djava.net.useSystemProxies=true -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar
    ```
2. Set a new value for the `credential.helper` configuration (essentially repeating the _manual configuration_ installation step, being careful with quoting and spaces), adding the appropriate properties.  For example, if you have a proxy server that can do HTTP and HTTPS, running on the host `192.168.0.117`, listening on port `8123`, then you would run the following (notice there's a pair of properties for http and one for https).


    ```
    git config --global credential.helper '!java -Ddebug=false -Dhttp.proxyHost=192.168.0.117 -Dhttp.proxyPort=8123 -Dhttps.proxyHost=192.168.0.117 -Dhttps.proxyPort=8123 -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar'
    ```


## How to remove or uninstall
We are sad to see you go!  Please give us some feedback on how we could do better next time.

### Uninstall from Mac using Homebrew or Linux using Linuxbrew

1. Run the GCM4ML in `uninstall` mode, which will update the "global" Git configuration file (the one in your home folder):

    ```
    git-credential-manager uninstall
    ```
2. Uninstall the git-credential-manager package with Homebrew/Linuxbrew:

    ```
    brew uninstall git-credential-manager
    ```
3. Archive the `insecureStore.xml` file from the `git-credential-manager` sub-folder under your HOME folder.


### Uninstall from Linux using RPM

1. Run the GCM4ML in `uninstall` mode, which will update the "global" Git configuration file (the one in your home folder):

    ```
    git-credential-manager uninstall
    ```
2. Uninstall the git-credential-manager package with RPM:

    ```
    sudo rpm --erase git-credential-manager-2.0.4-1.noarch
    ```
3. Archive the `insecureStore.xml` file from the `git-credential-manager` sub-folder under your HOME folder.


### Automatic configuration removal (recommended)
1. Retrieve the value of the `credential.helper` configuration:

    ```
    git config --global --get credential.helper git-credential-manager
    ```
2. Run the GCM4ML in `uninstall` mode, which will update the "global" Git configuration file (the one in your home folder):

    ```
    java -jar /home/example/git-credential-manager/git-credential-manager-2.0.4.jar uninstall
    ```
3. The value retrieved in _step 1_ contained the path to the JAR.  You can go delete that JAR.
4. Archive the `insecureStore.xml` file from the `git-credential-manager` sub-folder under your HOME folder.


### Manual configuration removal

1. Retrieve the value of the `credential.helper` configuration:

    ```
    git config --global --get credential.helper git-credential-manager
    ```
2. Delete the value of the `credential.helper` configuration:

    ```
    git config --global --unset credential.helper git-credential-manager
    ```
3. The value retrieved in _step 1_ contained the path to the JAR.  You can go delete that JAR.
4. Archive the `insecureStore.xml` file from the `git-credential-manager` sub-folder under your HOME folder.
