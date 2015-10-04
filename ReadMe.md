Microsoft Git Credential Manager for Java
=========================================
The Microsoft Git Credential Manager for Java is fairly simple. It stores and retrieves credentials for accessing Git resources on Visual Studio Online to and from a secure container.

How does it work?
-----------------
Once configured with Git, if Git needs credentials for reading from or writing to a Git remote, it sends a request to the program(s) configured as `credential.helper`, as described in [gitcredentials](http://git-scm.com/docs/gitcredentials.html).  If none of the credential helpers have valid credentials, Git will prompt for a username and password and then ask the credential helper(s) to save the values for later retrieval.

The first release of the Microsoft Git Credential Manager for Java works a lot like [git-credential-store](http://git-scm.com/docs/git-credential-store), writing and reading credentials from the file `insecureStore.xml` in your HOME folder.  You can make this file more secure by removing access to it from other users and turning on per-file encryption if your system supports it.

Upcoming releases will support OAuth2 to request authorization from Visual Studio Online, generate least-privilege Personal Access Tokens and then store these using the operating system's secure storage facility.
