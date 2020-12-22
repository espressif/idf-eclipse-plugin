# Contribution Guide (Developers)

## Setting up an Eclipse Development Environment
* Install `Java SE`(Java 11 and above). Here is the download link https://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install `Eclipse for RCP and RAP Developers` package (Eclipse 2020-06 and above) which you can find here https://www.eclipse.org/downloads/packages/
* Install `Eclipse CDT` plugins in the eclipse http://download.eclipse.org/tools/cdt/releases/9.6. (Choose compatible CDT version based on the Eclipse Release)
* Install `Eclipse Embedded CDT` package for OpenOCD https://projects.eclipse.org/projects/iot.embed-cdt/downloads
* Install `m2eclipse` plugins in the eclipse using update site https://download.eclipse.org/technology/m2e/releases/latest/
* Install `cmakeed` plugins in the eclipse https://raw.githubusercontent.com/15knots/cmakeed/master/cmakeed-update/ to get the CMake editor features
* Install latest `Eclipse SWTChart` using the update site https://projects.eclipse.org/projects/science.swtchart/downloads
* Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.
* Eclipse might prompt a wizard to install `Maven Plugin Connectors` to resolve the idf-eclipse-plugins maven errors, make sure you install all of them.

## How to contribute
* Clone this repo https://github.com/espressif/idf-eclipse-plugin.git
* Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
* Make changes locally on a specific local branch
* Test with Maven Tycho using `$ mvn clean verify`
* Submit a Pull Request(PR)
* Follow the standard contribution guidelines https://docs.espressif.com/projects/esp-idf/en/latest/contribute/index.html
