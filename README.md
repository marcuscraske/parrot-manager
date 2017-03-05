# parrot
A simple password manager, intended to securely share password databases across machines.

## features
- File and partial in-memory encryption of sensitive information (passwords, files, text)
- Sync databases using SSH, with automatic merge to avoid conflicts and losing data


## getting started
Dependencies:
- Requires JRE 1.8 or greater - [download](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

## installation
### ubuntu/debian
Download the latest deb file, to anywhere, and run the following command in the same directory; the file name
may differ:

````
sudo dpkg -i parrot-manager.deb
````

### windows
Download the latest Windows zip file and extract the executable where needed. No installation process is required,
as the application is standalone.

### jar
Download the latest zip or tar archive, extract the jar file and run:

````
java -jar parrot-manager.jar
````

File name may differ and no installation process is required, as the JAR is standalone.


## contribute
- Raise any suggestions or bugs as issues
- This project is open to pull requests

This project uses Java, JavaFX WebView and AngularJS 2; as well as many front-end libraries, managed by `npm`. Each
module should have its own `README.md` file for purpose.

### main
The entire project, including distributions, can be built using:

````
mvn clean package
````

### front-end / angularjs2
You can launch the node lite server to build the front-end on the fly, although parts are limited / not mocked:

````
./run-angular.sh
````

This will require `npm` v3 to be installed.
    
### development mode
If you need the ability to refresh files from the `target` directory from your IDE, launch `parrot-manager` using the
following arg:

````
--development=true
````
