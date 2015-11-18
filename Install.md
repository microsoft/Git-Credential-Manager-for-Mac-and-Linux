Installation Instructions
=========================
This document explains where to copy the JAR and how to configure Git to use the Git Credential Manager for Mac and Linux (GCM) as its credential helper.

System Requirements
-------------------
Great care was taken to avoid using any features of Java that would impact compatibility with Java 6.  Unfortunately, at this time, only the JavaFX-based browser is available, which excludes Java 6.  If you find a compatibility issue, please report it and provide as many details about your platform as necessary to reproduce the problem.

1. Mac OS X version 10.10.5 and up OR a GNU/Linux distribution with a desktop environment.
2. Oracle Java 7 Update 6 and up, Oracle Java 8, or OpenJDK with OpenJFX.
3. Git version 1.9 and up.
   1. On Mac OS X, the [Homebrew](http://brew.sh/) distribution is highly recommended.


How to install
--------------
Starting with version 1.1.0, the GCM is capable of configuring Git automatically, after double-checking its requirements.

### Automatic installation (recommended)

1. Copy the `${project.artifactId}-${project.version}.jar` file somewhere safe and stable, such as `~/${project.artifactId}/`.
2. Launch the JAR in `install` mode, which will check its requirements and then update the "global" Git configuration file (the one in your home folder):

    ```
    java -jar /home/example/${project.artifactId}/${project.artifactId}-${project.version}.jar install
    ```

### Homebrew installation

1. Install the needed resources using Homebrew:

    ```
    brew install ${project.artifactId}
    ```
2. Run ${project.artifactId} in `install` mode, which will check its requirements and then update the "global" Git configuration file (the one in your home folder):

    ```
    ${project.artifactId} install
    ```
    
### RPM installation

1. Copy the `${project.artifactId}-${project.version}-1.noarch.rpm` file somewhere locally.
2. Install the RPM running as the root user:

    ```
    sudo rpm -i ${project.artifactId}-${project.version}-1.noarch.rpm
    ```
3. Run ${project.artifactId} in `install` mode, which will check its requirements and then update the "global" Git configuration file (the one in your home folder):

    ```
    ${project.artifactId} install
    ```

### Manual installation

1. Copy the `${project.artifactId}-${project.version}.jar` file somewhere safe and stable, such as `~/${project.artifactId}/`.
2. Configure the `credential.helper` setting to launch Java with the absolute path to the JAR (make sure you surround the whole value with 'single quotes'):

    ```
    git config --global credential.helper '!java -Ddebug=false -jar /home/example/${project.artifactId}/${project.artifactId}-${project.version}.jar'
    ```


How to enable (or disable) debug mode
-------------------------------------
Debug mode will turn on tracing and assertions, producing a lot of output to `stderr`.  Only turn this on temporarily, when trying to isolate a defect.

1. Retrieve the value of the `credential.helper` configuration: `git config --global --get credential.helper ${project.artifactId}` ...it should look like this: `!java -Ddebug=false -jar /home/example/${project.artifactId}/${project.artifactId}-${project.version}.jar`
2. Set a new value for the `credential.helper` configuration (essentially repeating _manual installation step 2_, being careful with quoting and spaces), changing the value of the `debug` property to `true` (or `false` to disable).


How to remove or uninstall
--------------------------
We are sad to see you go!  Please give us some feedback on how we could do better next time.

### Automatic uninstallation (recommended)

1. Retrieve the value of the `credential.helper` configuration: `git config --global --get credential.helper ${project.artifactId}`
2. Launch the JAR in `uninstall` mode, which will update the "global" Git configuration file (the one in your home folder): `java -jar /home/example/${project.artifactId}/${project.artifactId}-${project.version}.jar uninstall`
3. The value retrieved in _step 1_ contained the path to the JAR.  You can go delete that JAR.
4. Archive the `insecureStore.xml` file from the `${project.artifactId}` sub-folder under your HOME folder.

### Homebrew uninstallation

1. Run ${project.artifactId} in `uninstall` mode, which will update the "global" Git configuration file (the one in your home folder):

    ```
    ${project.artifactId} uninstall
    ```
2. Uninstall the ${project.artifactId} package with Homebrew: `brew uninstall ${project.artifactId}`
3. Archive the `insecureStore.xml` file from the `${project.artifactId}` sub-folder under your HOME folder.

### RPM uninstallation

1. Run ${project.artifactId} in `uninstall` mode, which will update the "global" Git configuration file (the one in your home folder):

    ```
    ${project.artifactId} uninstall
    ```
2. Uninstall the ${project.artifactId} package with RPM: `sudo rpm -e ${project.artifactId}-${project.version}-1.noarch`
3. Archive the `insecureStore.xml` file from the `${project.artifactId}` sub-folder under your HOME folder.

### Manual uninstallation

1. Retrieve the value of the `credential.helper` configuration: `git config --global --get credential.helper ${project.artifactId}`
2. Delete the value of the `credential.helper` configuration: `git config --global --unset credential.helper ${project.artifactId}`
3. The value retrieved in _step 1_ contained the path to the JAR.  You can go delete that JAR.
4. Archive the `insecureStore.xml` file from the `${project.artifactId}` sub-folder under your HOME folder.
