# parrot manager dev
This module keeps development code, where possible, away from actual builds of parrot manager.


## First Time Setup
Make sure you have the following installed:
* JDK 9
* npm (latest)

Install global npm packages by going to `parrot-manager/webapp` and running `npm run pre-install`.


## Intellij and JDK 9+
When running parrot from Intellij, you will need to allow access to an internal API for WebView debugging.

Go to:

    Preferences > Build, Execution, Deployment > Compiler > Java Compiler.

Add the following to "Additional command line parameters":

    --add-exports javafx.web/com.sun.javafx.scene.web=ALL-UNNAMED


When compiling with the dev profile, use JDK 9. Otherwise use JDK 8, or you will likely see WebView is missing
when attempting to run within Intellij.


## Compiling
The entire project, including distributions, can be built using:

````
mvn clean package
````

You will need Angular CLI to build the front-end (next section).


## Front-end / AngularJS
You can launch the node lite server to build the front-end on the fly, although parts are limited / not mocked:

````
./run-angular.sh
````

This will require `npm` (at least v3) to be installed. You will need to install the Angular CLI; navigate to
`parrot-manager/webapp` and run `sudo npm run pre-install` or refer to Angular documentation.


## Dev Profile
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
when using the `run-angular.sh` script (see next section). If you need to load files off the class-path, start parrot
with the following argument:

````
--classpath=true
````


## Docs
More documentation can be found within each module.
