# Contribution Guide (Developers)

## Setting up an Eclipse Development Environment
* Install `Java SE`(Java 11 and above). Here is the download link https://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install `Eclipse for RCP and RAP Developers` package (Eclipse 2020-09 and above) which you can find here https://www.eclipse.org/downloads/packages/
* Install `Eclipse CDT` plugins in the eclipse https://download.eclipse.org/tools/cdt/releases/latest/ (Choose compatible CDT version based on the Eclipse Release)
* Install `Eclipse Embedded CDT` package for OpenOCD https://projects.eclipse.org/projects/iot.embed-cdt/downloads
* Install `m2eclipse` plugins in the eclipse using update site https://download.eclipse.org/technology/m2e/releases/latest/
* Install `cmakeed` plugins in the eclipse https://raw.githubusercontent.com/15knots/cmakeed/master/cmakeed-update/ to get the CMake editor features
* Install latest `Eclipse SWTChart` using the update site https://projects.eclipse.org/projects/science.swtchart/downloads
* Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.
* Eclipse might prompt a wizard to install `Maven Plugin Connectors` to resolve the idf-eclipse-plugins maven errors, make sure you install all of them.
* Install `SWTBot` using the update site https://download.eclipse.org/technology/swtbot/releases/latest/
* Install latest `nebula` plugins using the update site http://download.eclipse.org/nebula/releases/latest

## How to contribute
* Clone repo `git clone https://github.com/espressif/idf-eclipse-plugin.git`
* Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
* Make changes locally on a specific local branch
* Test with Maven Tycho using `$ mvn clean verify -Djarsigner.skip=true`
* Submit a Pull Request(PR)
* It is also recommended that you add or update a Functional Test if you are adding or updating a functionality in plugin. More details about adding SWTBot Funtional test can be found in the README.md in test folder in the repo.

## Coding Standards and guidelines
* Code formatter https://github.com/espressif/idf-eclipse-plugin/blob/master/resources/espressif_eclipse_formatter.xml
* Code quality https://marketplace.eclipse.org/content/findbugs-eclipse-plugin
* Contribution guidelines https://docs.espressif.com/projects/esp-idf/en/latest/contribute/index.html
