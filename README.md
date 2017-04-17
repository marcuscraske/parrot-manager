# parrot
<p align="center">
    <img src="parrot-manager/src/main/resources/icons/parrot-icon.png" alt="parrot logo" />
</p>

[![Build Status](https://travis-ci.org/limpygnome/parrot-manager.svg?branch=master)](https://travis-ci.org/limpygnome/parrot-manager)

A simple password manager with a modern interface, with ability to synchronize databases using SSH.

## Features
- File encryption
- In-memory encryption (until revealed/copied/etc)
- Automatic backups on save
- Sync databases using SSH
    - Multiple hosts at once
    - Automatic merge to avoid conflicts and losing data
- Copy values to clipboard
- Send values as keys to other applications
- Modern UI

## Prerequisites
- Requires at least Java Runtime 1.8 - [download](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

## Installation
### Ubuntu / Debian
Download the latest deb file, to anywhere, and run the following command in the same directory; the file name
may differ:

````
sudo dpkg -i parrot-manager.deb
````

### Windows
Download the latest Windows zip file and extract the executable where needed. No installation process is required,
as the application is standalone.

### JAR
Download the latest zip or tar archive, extract the jar file and run:

````
java -jar parrot-manager.jar
````

File name may differ and no installation process is required, as the JAR is standalone.


## Contribute
- Issues - raise any suggestions or bugs, or help others
- This project is open to pull requests

This project uses Java, JavaFX WebView and AngularJS 2; as well as many front-end libraries, managed by `npm`. Each
module should have its own `README.md` file for purpose.

### Main
The entire project, including distributions, can be built using:

````
mvn clean package
````

### Front-end / angularjs2
You can launch the node lite server to build the front-end on the fly, although parts are limited / not mocked:

````
./run-angular.sh
````

This will require `npm` v3 to be installed.
    
### Development Mode
If you need the ability to refresh files from the `target` directory from your IDE, launch `parrot-manager` using the
following arg:

````
--development=true
````

**Note**: Make sure the working directory is set to `parrot-manager` sub-directory.
