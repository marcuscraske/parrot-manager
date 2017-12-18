# parrot manager dev
This module keeps development code, where possible, away from actual builds of parrot manager.

## Intellij
When running parrot from Intellij, you will need to allow access to an internal API for WebView debugging.

Go to:

    Preferences > Build, Execution, Deployment > Compiler > Java Compiler.

Add the following to "Additional command line parameters":

    --add-exports javafx.web/com.sun.javafx.scene.web=ALL-UNNAMED


### JDK 9
When compiling with the dev profile, use JDK 9. Otherwise use JDK 8, or you will likely see WebView is missing
when attempting to run within Intellij.
