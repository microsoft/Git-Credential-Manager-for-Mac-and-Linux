Git Credential Manager for Mac and Linux
========================================
The Git Credential Manager for Mac and Linux (GCM) is fairly simple. It stores and retrieves credentials for accessing Git resources on Visual Studio Online (VSO) to and from a secure container.

How does it work?
-----------------
Once configured with Git, if Git needs credentials for reading from or writing to a Git remote, it sends a request to the program(s) configured as `credential.helper`, as described in [gitcredentials](http://git-scm.com/docs/gitcredentials.html).  If none of the credential helpers have valid credentials, Git will prompt for a username and password and then ask the credential helper(s) to save the values for later retrieval.

The GCM currently stores credentials in the file `insecureStore.xml`, located in the `git-credential-manager` sub-folder under your HOME folder.  You can make this file more secure by turning on file or folder encryption, if your system supports it.  An upcoming release will use the operating system's secure storage facility when it's available.

If you are connecting to a Git repository hosted in a VSO account, the GCM will open a web browser window so you can authenticate and authorize access to your account (via OAuth 2.0), allowing the credential manager to then use the access token to create a VSO Personal Access Token (PAT) scoped for `vso.code_write`, effectively granting the GCM permission to read and write to your Git repositories.

If you are connecting to Git repositories hosted elsewhere, the GCM works a lot like [git-credential-store](http://git-scm.com/docs/git-credential-store) and will store & retrieve your username & password.
