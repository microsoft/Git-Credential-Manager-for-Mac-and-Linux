Git Credential Manager for Mac and Linux
========================================
Stores credentials for Git version control securely.
    Provides secure logon for Visual Studio Team Services (visualstudio.com).


License
-------
This source code and artifacts are released under the terms of the [MIT License](http://www.opensource.org/licenses/mit-license.php). 


Build status
------------
This project has continuous integration hosted by Travis CI:
[![Build Status](https://travis-ci.org/Microsoft/Git-Credential-Manager-for-Mac-and-Linux.svg?branch=master)](https://travis-ci.org/Microsoft/Git-Credential-Manager-for-Mac-and-Linux)


How does it work?
-----------------
Once configured with Git, if Git needs credentials for reading from or writing to a Git remote, it sends a request to the program(s) configured as `credential.helper`, as described in [gitcredentials](http://git-scm.com/docs/gitcredentials.html).  If none of the credential helpers have valid credentials, Git will prompt for a username and password and then ask the credential helper(s) to save the values for later retrieval.

On Mac OS X, the GCM stores credentials in the Keychain.  On Linux, the GCM currently stores credentials in the file `insecureStore.xml`, located in the `git-credential-manager` sub-folder under your HOME folder.  You can make this file more secure by turning on file or folder encryption, if your system supports it.  Support for the GNOME Keyring is planned.

If you are connecting to a Git repository hosted in a VSTS account, the GCM will attempt to open a web browser window so you can authenticate and authorize access to your account (via OAuth 2.0).  If a web browser cannot be opened (because the system is headless, accessed via SSH or doesn't have the required components), instructions will be provided to use a web browser on another device (via OAuth 2.0 Device Flow) so you can authenticate and authorize access to your account.   In either case, the credential manager will then use the access token to create a VSTS Personal Access Token (PAT) scoped for `vso.code_write`, effectively granting Git permission to read and write to your Git repositories hosted in VSTS.

If you are connecting to Git repositories hosted elsewhere, the GCM works a lot like [git-credential-store](http://git-scm.com/docs/git-credential-store) and will store & retrieve your username & password.


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


How can I find out more?
------------------------
Visit the [Git Credential Manager](https://java.visualstudio.com/Docs/tools/gitcredentialmanager) page or browse the [source code on GitHub](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux).
