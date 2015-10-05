Installation Instructions
=========================
This document explains where to copy the JAR and how to configure Git to use the "Microsoft Git Credential Manager for Java" as its credential helper. 

System Requirements
-------------------
Great care was taken to avoid using any features of Java that would impact compatibility with Java 6.  If you find a compatibility issue, please report it and provide as many details about your platform as necessary to reproduce the problem.
1. Java 6+
2. Git 1.9+

How to install
--------------
1. Copy the `git-credential-helper-0.1.3.jar` file somewhere safe and stable, such as `%APPDATA%/git-credential-helper/` on Windows or `~/Library/Application Support/git-credential-helper/` on Mac or `~/git-credential-helper/` on Linux.
2. Configure the `credential.helper` setting to launch Java with the absolute path to the JAR (make sure you surround the whole value with 'single quotes' in Bash and "double quotes" in the Windows Command Prompt, _as well as_ either double-quoting values containing spaces or escaping the spaces with a backslash):

```git config --global credential.helper '!java -Ddebug=false -jar /path/to\ folder\ with\ spaces/git-credential-helper-0.1.3.jar'```

How to enable (or disable) debug mode
-------------------------------------
Debug mode will turn on tracing and assertions, producing a lot of output to `stderr`.  Only turn this on temporarily, when trying to isolate a defect.
1. Retrieve the value of the `credential.helper` configuration:
 
 ```git config --global --get credential.helper```
 
 ...it should look like this:
 
 ```!java -Ddebug=false -jar /path/to/git-credential-helper-0.1.3.jar```
2. Set a new value for the `credential.helper` configuration (essentially repeating _installation step 2_, being careful with quoting and spaces), changing the value of the `debug` property to `true` (or `false` to disable).

How to remove or uninstall
--------------------------
We are sad to see you go!  Please give us some feedback on how we could do better next time.
1. Retrieve the value of the `credential.helper` configuration:
 
 ```git config --global --get credential.helper```
2. Delete the value of the `credential.helper` configuration:
 
 ```git config --global --unset credential.helper```
3. The value retrieved in _step 1_ contained the path to the JAR.  You can go delete that JAR.
4. Archive the `insecureStore.xml` file from your HOME folder. 
