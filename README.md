# idf-eclipse-plugin

Eclipse IDE for Espressif IoT Development Framework

## Download and Installation:

1. Download the artifacts.zip file from https://gitlab.espressif.cn:6688/idf/idf-eclipse-plugin/-/jobs/artifacts/master/download?job=build
2. Extract the above downloaded zip file
3. Download Eclipse CDT from here https://www.eclipse.org/downloads/packages/release/2018-12/r/eclipse-ide-cc-developers
4. Launch Eclipse
5. Go to `Help` -> `Install New Software`
6. Click `Add…` button
7. Select `Achieve` from Add repository dialog and select the file ``com.espressif.idf.update-1.0.0-SNAPSHOT.zip`` from the exacted folder
8. Click `Add`
9. Select `Espressif IDF` from the list and proceed with the installation 
10. Restart the Eclipse

## To create a new Project using default esp-idf-template:
1. Go to `File` Menu > `Espressif IDF Project`
2. Provide the project Name
3. Click `Finish`

## To create a new project using idf examples/templates:
1. Go to `File` Menu > `Espressif IDF Project`
2. Provide the project Name
3. Click `Next`
4. Check `Create a project using one of the templates`
5. Select the required template from the tree
6. Click `Finish`
