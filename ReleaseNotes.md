Release Notes
=============
Every release is described here, from latest to earliest.


Version 1.1.0
-------------
### Minor changes
- Added support for Visual Studio Online (VSO) accounts associated with Microsoft Accounts (a.k.a. "MSA", "Windows Live ID", etc.).  The user experience should be the same as the VSO accounts associated with Azure Active Directory (AAD).
- Added a self-install (with self-check) and self-uninstall.
- Implemented (cross-site request forgery) CSRF prevention during the OAuth 2.0 authorization request as recommended by RFC 6749.

### Known issues
- When using with another credential helper (on Mac OS X, with the default Git distribution known as "Apple Git", the `credential-osxkeychain` helper is _hardcoded_), if the other helper is asked _before_ this one and the credentials are not valid (because they have expired or have been revoked), Git asks _all_ the helpers to erase whatever credentials they have for that server and then aborts with "Authentication failed".
 - **Workaround:** move the other helper(s) _after_ the GCM, disable the other helper(s) or, if the latter is not possible (it's not possible to disable the `credential-osxkeychain` helper on Mac when using "Apple Git"), then install another Git distribution.
- Versions of Java before Oracle Java 7 Update 6 as well as default OpenJDK installations currently do not support OAuth 2.0 authentication & authorization with Visual Studio Online.
 - **Workaround:** install Oracle Java 7 Update 6 (or later) or [build & install OpenJFX for OpenJDK 8](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX).


Version 1.0.0
-------------
### Major changes
- Visual Studio Online (VSO) accounts associated with Azure Active Directory (AAD) are now supported.  Users will be presented with a web browser to participate in an [OAuth 2.0 flow](http://tools.ietf.org/html/rfc6749#section-4.1), allowing the credential manager to generate a VSO Personal Access Token (PAT) for use when pulling from and pushing to VSO Git repositories.
- The `insecureStore.xml` file has been moved to its own folder (~/git-credential-manager/) and will now have permissions set so only the owner can read and write it.
- The API has had all traces of asynchronous results removed. 

### Minor changes
- A fatal error or an authentication failure now aborts the process, asking Git to stop invoking credential helpers.
- Details of any fatal errors are now sent to _standard error_, making them visible without having turn on the debug mode.

### Known issues
- When using with another credential helper (on Mac OS X, with the default Git distribution known as "Apple Git", the `credential-osxkeychain` helper is _hardcoded_), if the other helper is asked _before_ this one and the credentials are not valid (because they have expired or have been revoked), Git asks _all_ the helpers to erase whatever credentials they have for that server and then aborts with "Authentication failed".
 - **Workaround:** move the other helper(s) _after_ the GCM, disable the other helper(s) or, if the latter is not possible (it's not possible to disable the `credential-osxkeychain` helper on Mac when using "Apple Git"), then install another Git distribution.
- Visual Studio Online accounts not associated with Azure Active Directory (i.e. using a Microsoft Account) are not yet supported.
- Versions of Java before Oracle Java 7 Update 6 as well as default OpenJDK installations currently do not support OAuth 2.0 authentication & authorization with Visual Studio Online.
 - **Workaround:** install Oracle Java 7 Update 6 (or later) or [build & install OpenJFX for OpenJDK 8](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX).


Version 0.1.3
-------------
Improvements to the documentation.


Version 0.1.2
-------------
Initial release of C# port which supports Basic authority only and a plain-text credential store (`~/insecureStore.xml`) making it equivalent to the built-in git-credential-store.
