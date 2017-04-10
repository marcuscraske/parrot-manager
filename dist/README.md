# dist
This module contains sub-modules for creating distribution packages, of parrot-manager, and deploying those packages.

As these modules are too small, the README will be left here.

Overall purpose of each module:
- `debian` - creates standard deb package for Linux distributions with `dpkg` available, which is also required to build the module.
- `windows` - creates zip archive for Windows executable.
- `jar` - creates a zip and tar archive with a JAR executable, for alternative platforms such as Mac.
- `github` - deploys all previously mentioned distributions to Github as apart of a release.
