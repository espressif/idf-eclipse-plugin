# Contribution Guide (Developers)

## Setting up an Eclipse Development Environment
* Install `Java SE`(Java 8 and above). Here is the download link https://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install `Eclipse for RCP and RAP Developers` package (Eclipse 2018-12 and above) which you can find here https://www.eclipse.org/downloads/packages/
* Install `CDT` plugins in the eclipse http://download.eclipse.org/tools/cdt/releases/9.6. (Choose compatible CDT version based on the Eclipse Release) 
* Install `m2eclipse` plugins in the eclipse https://www.eclipse.org/m2e/
* Install `cmakeed` plugins in the eclipse https://raw.githubusercontent.com/15knots/cmakeed/master/cmakeed-update/ to get the CMake editor features
* Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.
* Eclipse might prompt a wizard to install `Maven Plugin Connectors` to resolve the idf-eclipse-plugins maven errors, make sure you install all of them.

## How to contribute
* Clone this repo https://github.com/espressif/idf-eclipse-plugin.git
* Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
* Make changes locally on a specific local branch
* Test with Maven Tycho using `$ mvn clean verify`
* Submit a Merge Request(MR)
* Follow the standard contribution guidelines https://docs.espressif.com/projects/esp-idf/en/latest/contribute/index.html
