These notes are for release **2.0.0**.
Other releases and their notes can be found at the [Git-Credential-Manager-for-Mac-and-Linux GitHub Releases](https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/releases) page.

* Major:
    * Implemented OAuth 2.0 Device Flow according to [draft-ietf-oauth-device-flow-01](https://tools.ietf.org/html/draft-ietf-oauth-device-flow-01).  Made possible via pull requests #38, #39 and #40.
    * Relaxed system requirements such that Java 1.6+ is supported.  Made possible via pull request #41.
    * The workaround for the "hardcoded credential-osxkeychain" issue is now enabled by default.  Implemented in pull request #47.
    * Added support for the GNOME Keyring on GNU/Linux.  Made possible via pull request #49.
* Minor:
    * Minor documentation improvements via pull request #43.
    * Minor unit test improvements via pull request #44.
    * Reduced the amount of debug output when JavaFX can't be used, via pull request #45.
    * Updated the Azure Active Directory client ID via pull request #48.
