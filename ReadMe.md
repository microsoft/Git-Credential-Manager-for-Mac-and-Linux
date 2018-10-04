Git Credential Manager for Mac and Linux
========================================
Stores credentials for Git version control securely.
    Provides secure logon for Visual Studio Team Services (https://visualstudio.com).


License
-------
This source code and artifacts are released under the terms of the [MIT License](https://opensource.org/licenses/mit-license.php). 
The binary distribution (`git-credential-manager-2.0.4.jar`) is released under the terms of the Git Credential Manager for Mac and Linux software license.


Build status
------------
This project has continuous integration hosted by Travis CI:
[![Build Status](https://travis-ci.org/Microsoft/Git-Credential-Manager-for-Mac-and-Linux.svg?branch=master)](https://travis-ci.org/Microsoft/Git-Credential-Manager-for-Mac-and-Linux)


How does it work?
-----------------
Once configured with Git, if Git needs credentials for reading from or writing to a Git remote, it sends a request to the program(s) configured as `credential.helper`, as described in [gitcredentials](https://git-scm.com/docs/gitcredentials.html).  If none of the credential helpers have valid credentials, Git will prompt for a username and password and then ask the credential helper(s) to save the values for later retrieval.

On Mac OS X, the GCM4ML stores credentials in the Keychain.  On Linux, the GCM4ML stores credentials in the GNOME Keyring.  If you used an older version of the GCM4ML that stored credentials in the `insecureStore.xml` file, its contents will be imported into secure storage on first run and then the file will be renamed to `insecureStore.xml.old`.  Once you are satisfied you will no longer need to downgrade the GCM4ML, you can delete `insecureStore.xml.old`.

If you are connecting to a Git repository hosted in a Visual Studio Team Services (VSTS) account, the GCM4ML will attempt to open an internal web browser window so you can authenticate and authorize access to your account (via OAuth 2.0).  If a web browser cannot be opened (this usually happens because the system doesn't have the required components), instructions will be provided to use any external web browser (via OAuth 2.0 Device Flow) so you can authenticate and authorize access to your account.  In either case, the credential manager will then use the access token to create a VSTS Personal Access Token (PAT) scoped for `vso.code_write`, effectively granting Git permission to read and write to your Git repositories hosted in VSTS.

If you are connecting to Git repositories hosted elsewhere, the GCM4ML works a lot like [git-credential-store](https://git-scm.com/docs/git-credential-store) and will store & retrieve your username & password.

### Data collection
There are no telemetry nor crash-reporting features in the GCM4ML.  Aside from the interactions with Visual Studio Team Services (VSTS) from your device and under your account, the only data collected are the following pieces of non-personally-identifiable information in the [user-agent string](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/blob/master/src/main/java/com/microsoft/alm/authentication/Global.java):

1. Operating System name
2. Operating System version
3. Operating System architecture
4. Java Virtual Machine name
5. Java Virtual Machine version
6. GCM version

For example:

`git-credential-manager (Mac OS X; 10.10.5; x86_64) Java HotSpot(TM) 64-Bit Server VM/1.8.0_92-b14 git-tools/2.0.4`

The collection of this data is strictly for statistical purposes and is governed by the [Microsoft Visual Studio Product Family Privacy Statement](https://go.microsoft.com/fwlink/?LinkId=528096&clcid=0x409).

How do I install it?
--------------------
Follow the instructions in [Install.md](Install.md).


How do I build it?
------------------
If you have version 6 or better of the JDK, as well as version 3 or better of Maven, you're all set!  Run the following:

    mvn clean verify

This will download the dependencies, compile the code, run unit tests, and package everything.  You should end up with a file named something like `git-credential-manager-VERSION.jar` under the `target` sub-folder.


How can I contribute?
---------------------
Please refer to [Contributing.md](Contributing.md).


Reporting Security Vulnerabilities
----------------------------------
If you believe you have found a security vulnerability in this project, please follow [these steps](https://technet.microsoft.com/en-us/security/ff852094.aspx) to report it. For more information on how vulnerabilities are disclosed, see [Coordinated Vulnerability Disclosure](https://technet.microsoft.com/en-us/security/dn467923).


Code of Conduct
---------------
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.


How can I find out more?
------------------------
Visit the [Git Credential Manager](https://docs.microsoft.com/en-us/azure/devops/repos/git/set-up-credential-managers) page or browse the [source code on GitHub](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux).
