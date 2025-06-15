[![GitHub release](https://img.shields.io/github/release/espressif/idf-eclipse-plugin.svg)](https://github.com/espressif/idf-eclipse-plugin/releases/latest)

[中文](./README_CN.md)

# Espressif-IDE (ESP-IDF Eclipse Plugin)

Espressif-IDE is an Integrated Development Environment (IDE) based on Eclipse CDT for developing IoT Applications using the <a href=https://github.com/espressif/esp-idf>ESP-IDF</a>. It’s a standalone and customized IDE built specifically for ESP-IDF. Espressif-IDE comes with the IDF Eclipse plugins, essential Eclipse CDT plugins, and other third-party plugins from the Eclipse platform to support building ESP-IDF applications.

The plug-in runs on `macOS`, `Windows` and `Linux` platforms.

![](docs_readme/images/macos-logo.png)
![](docs_readme/images/windows-logo.png)
![](docs_readme/images/linux-logo.png)


> **Note:** Espressif-IDE version 3.0 and later supports ESP-IDF version 5.x and above. For ESP-IDF version 4.x, please use Espressif-IDE version <a href="https://github.com/espressif/idf-eclipse-plugin/releases/tag/v2.12.1">2.12.1</a>

# To Get Started with the Espressif-IDE

Please refer to the <a href="https://docs.espressif.com/projects/espressif-ide/en/latest/">Espressif-IDE documentation</a>.

# How to Build Locally

1. Install prerequisites: Java 17+ and Maven.
2. Run the below commands to clone and build.

	```
	git clone https://github.com/espressif/idf-eclipse-plugin.git
	cd idf-eclipse-plugin
	mvn clean verify -Djarsigner.skip=true \
	  -Djdk.xml.maxGeneralEntitySizeLimit=0 \
	  -Djdk.xml.totalEntitySizeLimit=0
	```

This will generate p2 update site artifact:

* Name: `com.espressif.idf.update-*`
* Location: `releng/com.espressif.idf.update/target`

This artifact can be installed using the mechanism mentioned <a href="https://docs.espressif.com/projects/espressif-ide/en/latest/marketplaceupdate.html?#installing-idf-eclipse-plugin-from-local-archive">here</a>

# How to Get the Latest Development Build

1. Go to the last commit of the master branch <a href="https://github.com/espressif/idf-eclipse-plugin/commits/master">here</a>.
1. Click on a :white_check_mark: green tick mark.
1. Click on `Details`.
1. Click on `Summary` on the left.
1. Scroll down to see the `Artifacts` section.
1. Download `com.espressif.idf.update` p2 update site archive and install as per the instructions mentioned <a
href="https://docs.espressif.com/projects/espressif-ide/en/latest/marketplaceupdate.html?#installing-idf-eclipse-plugin-from-local-archive">here</a>.


<a name="Support"></a>
# How to Raise Bugs

Please raise the issues [here](https://github.com/espressif/idf-eclipse-plugin/issues) with the complete environment details and log.
