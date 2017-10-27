# Contribute

Recommended ways to contribute:
- Issues - raise any suggestions or bugs, or help others
- This project is open to pull requests, new ideas and fixes welcome!

This project uses Java, JavaFX WebView and AngularJS; as well as many front-end libraries, managed by `npm`. Each
module should have its own `README.md` file for purpose.

A list of ideas and upcoming features can be found in the [TODO](todo.md).

## Main Application
The entire project, including distributions, can be built using:

````
mvn clean package
````

### Development Mode
If you need the ability to refresh files from the `target` directory from your IDE, launch `parrot-manager` using the
following arg:

````
--development=true
````

**Note**: Make sure the working directory is set to `parrot-manager` sub-directory.


## Front-end / AngularJS
You can launch the node lite server to build the front-end on the fly, although parts are limited / not mocked:

````
./run-angular.sh
````

This will require `npm` (at least v3) to be installed.

## Docs
More documentation can be found within each module.
