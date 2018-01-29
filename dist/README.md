# dist
This module contains sub-modules for creating various distributions of parrot-manager, which are pushed to Github
using the project's version as the release tag.

- `linux-debian` - creates standard deb package for Linux distributions with `dpkg` available, which is also required to build the module.
- `windows` - creates an msi installer for Windows.
- `windows-standalone` - creates zip archive containing a Windows executable for stnadalone mode.
- `mac` - creates a pkg file to install parrot manager.
- `jar` - creates a zip and tar archive with a JAR executable, for alternative platforms such as Mac.
- `jar-standalone` - prepares baked standalone version of parrot.
- `jar-standalone-deploy` - creates a standalone jar of parrot, compressed in zip and tar.gz formats.
- `github` - deploys all previously mentioned distributions to Github as apart of a release.


## Release
Start by creating a release on Github:

````
./_start-release.sh
````

And run the build script for each platform:

````
./build-mac.sh
````

````
./build-linux.sh
````

````
./build-windows.bat
````
