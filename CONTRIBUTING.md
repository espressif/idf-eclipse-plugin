# Contribution Guide (Developers)

## Setting up an Eclipse Development Environment using IDF Target (Recommended approach)
* Navigate to `com.espressif.idf.target` plugin
* Click on `com.espressif.idf.target.target` file
* Click on `Set as Active Target Platform` and wait for a couple of mins to download and configure your environment with the idf required plugins
  
## Setting up an Eclipse Development Manually
* Install `Java SE`(Java 17 and above) from https://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install `Eclipse for RCP and RAP Developers` package (Eclipse 2023-03 and above) from https://www.eclipse.org/downloads/packages/
* Install `Eclipse CDT` plugins in the eclipse https://download.eclipse.org/tools/cdt/releases/latest/ (Choose compatible CDT version based on the Eclipse Release)
* Install `Eclipse C/C++ OpenOCD Debugging` package from https://download.eclipse.org/embed-cdt/updates/v6/
* Install `m2eclipse` plugins in the eclipse using update site https://download.eclipse.org/technology/m2e/releases/latest/
* Install `cmakeed` plugins in the eclipse https://raw.githubusercontent.com/15knots/cmakeed/master/cmakeed-update/ to get the CMake editor features
* Install latest `Eclipse SWTChart` using the update site https://projects.eclipse.org/projects/science.swtchart/downloads
* Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.
* Eclipse might prompt a wizard to install `Maven Plugin Connectors` to resolve the idf-eclipse-plugins maven errors, make sure you install all of them.
* Install `SWTBot` using the update site https://download.eclipse.org/technology/swtbot/releases/latest/
* Install latest `nebula` plugins using the update site http://download.eclipse.org/nebula/releases/latest

## Common errors during installation process
* An API baseline has not been set for the current workspace.

![image](https://github.com/espressif/idf-eclipse-plugin/assets/69584713/945eac3c-a0f1-4277-bba1-7ca2a1e86499)

Fix: Window -> Preferences -> Plug-in Development -> API Baselines -> Missing API Baseline -> Change to "Warning"

* The type org.slf4j.Logger cannot be resolved. It is indirectly referenced from required type org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot	

![image](https://github.com/espressif/idf-eclipse-plugin/assets/69584713/0a879d4f-99e2-4d5a-98ca-a745a6683752)

Fix: in "Project Explorer" -> com.espressif.idf.tests -> com.espressif.idf.ui.test -> META-INF -> double-click MANIFEST.MF -> Dependencies -> Add... -> Type "slf4j.api" -> Add&Save. 

* After importing project Eclipse might prompt a wizard to install `Maven Plugin Connectors` to resolve the idf-eclipse-plugins maven errors, make sure you install all of them. But, depending on the version(Eclipse / Extensions), an error may occur:

![image](https://github.com/espressif/idf-eclipse-plugin/assets/69584713/048196c9-8ac6-4f10-8095-596ab2ef05f2)

Fix: check the error and delete one of the extension (usually Tycho) - Help -> Install New Software -> Already Installed -> select "Tycho" -> Uninstall. 

* Run as -> SWTBot Test -> may lead to error: 

![image](https://github.com/espressif/idf-eclipse-plugin/assets/69584713/3b563f57-d43e-4cce-a48b-5f9f9a490d7d)

Fix: Uninstall SWTBot -> Install latest snapshot(04.04.2023): http://download.eclipse.org/technology/swtbot/snapshots


## How to contribute
* Clone repo `git clone https://github.com/espressif/idf-eclipse-plugin.git`
* Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
* Make changes locally on a specific local branch
* Test with Maven Tycho using `$ mvn clean verify -Djarsigner.skip=true`
* Submit a Pull Request(PR)
* It is also recommended that you add or update a Functional Test if you are adding or updating a functionality in plugin. More details about adding SWTBot Functional test can be found in the README.md in test folder in the repo.

## Coding Standards and guidelines
* Code formatter https://github.com/espressif/idf-eclipse-plugin/blob/master/resources/espressif_eclipse_formatter.xml
* Code quality https://marketplace.eclipse.org/content/findbugs-eclipse-plugin
* Contribution guidelines https://docs.espressif.com/projects/esp-idf/en/latest/contribute/index.html
