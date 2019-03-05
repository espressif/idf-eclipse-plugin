# idf-eclipse-plugin

Eclipse IDE for Espressif IoT Development Framework(IDF)

## Setting up an Eclipse Development Environment:
1. Download and install `Java SE 11`(latest Java 11). Here is the download link https://www.oracle.com/technetwork/java/javase/downloads/index.html
2. Download and install `Eclipse RCP package` (latest Eclipse 2018-12) which you can find here https://www.eclipse.org/downloads/packages/
3. Install the `m2eclipse` feature into eclipse from the update site: https://www.eclipse.org/m2e/
4. Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.


## How to contribute:
1. Fork this repo 
2. Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
3. Make changes locally on your fork
4. Test with Maven Tycho using `$ mvn clean verify`
5. Submit a Merge Request(MR)


## Download and Installation:

1. Download `Eclipse CDT package` and install it. https://www.eclipse.org/downloads/packages/release/2018-12/r/eclipse-ide-cc-developers
2. Download the Espressif IDF eclipse `artifacts.zip` file from https://gitlab.espressif.cn:6688/idf/idf-eclipse-plugin/-/jobs/artifacts/master/download?job=build
3. Extract the above downloaded zip file
4. Launch Eclipse
5. Go to `Help` -> `Install New Software`
6. Click `Add…` button
7. Select `Achieve` from Add repository dialog and select the file ``com.espressif.idf.update-1.0.0-SNAPSHOT.zip`` from the exacted folder
8. Click `Add`
9. Select `Espressif IDF` from the list and proceed with the installation 
10. Restart the Eclipse


## To create a new Project using default esp-idf-template:
1. Make sure you're in C/C++ perspective.
2. Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`)
3. Provide the project Name
4. Click `Finish`


## To create a new project using idf examples/templates:
1. Make sure you're in C/C++ perspective.
2. Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`)
3. Provide the project Name
4. Click `Next`
5. Check `Create a project using one of the templates`
6. Select the required template from the tree
7. Click `Finish`

## Building the IDF projects

### Tools setup and infrastructure:
Please follow the instructions from here https://docs.espressif.com/projects/esp-idf/en/latest/get-started-cmake/index.html

### Configuring Eclipse for CMake IDF projects
Please follow the instructions from here https://cdtdoug.ca/2018/07/02/cdt-for-esp32.html


## How to raise bugs
Raise issues directly under espressif JIRA here https://jira.espressif.com:8443/projects/IEP/issues/ with the project name `IDF Eclipse Plugin(IEP)`

