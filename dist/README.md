# dist
This module contains sub-modules for creating various distributions of parrot-manager, which are pushed to Github
using the project's version as the release tag.

- `debian` - creates standard deb package for Linux distributions with `dpkg` available, which is also required to build the module.
- `windows` - creates an msi installer for Windows.
- `windows-standalone` - creates zip archive containing a Windows executable wrapper.
- `mac` - creates a pkg file to install parrot manager.
- `jar` - creates a zip and tar archive with a JAR executable, for alternative platforms such as Mac.
- `github` - deploys all previously mentioned distributions to Github as apart of a release.
