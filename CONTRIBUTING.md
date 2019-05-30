# Contribution Guide (Developers)

## Setting up an Eclipse Development Environment:
* Download and install `Java SE`(Java 8 and higher). Here is the download link https://www.oracle.com/technetwork/java/javase/downloads/index.html
* Download and install `Eclipse RCP package` (latest Eclipse 2018-12) which you can find here https://www.eclipse.org/downloads/packages/
* Install `CDT` feature into eclipse from the update site: http://download.eclipse.org/tools/cdt/releases/9.6. 
* Install the `m2eclipse` feature into eclipse from the update site: https://www.eclipse.org/m2e/
* Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.


## How to contribute:
* Fork this repo https://gitlab.espressif.cn:6688/idf/idf-eclipse-plugin
* Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
* Make changes locally on your fork
* Test with Maven Tycho using `$ mvn clean verify`
* Submit a Merge Request(MR)
