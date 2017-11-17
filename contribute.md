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

### Development
Enable the `dev` Maven profile for development features.

Example when building from command-line:

````
mvn clean package -Pdev
````

Or if you're using an IDE such as Intellij, use the `Maven Projects` (under `View` > `Tool Windows`) dialogue to
enable the profile.

If you need to perform debugging of the front-end, visit the following from Chrome:
<chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/>

The front-end is loaded from `http://localhost:3000` by default when using `dev` profile, which is the default port
when using the `run-angular.sh` script. If you need to load files off the class-path, start parrot with the following
argument:

````
--classpath=true
````

## Front-end / AngularJS
You can launch the node lite server to build the front-end on the fly, although parts are limited / not mocked:

````
./run-angular.sh
````

This will require `npm` (at least v3) to be installed.


## Docs
More documentation can be found within each module.
