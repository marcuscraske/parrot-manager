# Roadmap

## Upcoming
- Support building with JDK 11 (only)
    - Ensure bundle dist available for every platform with OpenJDK jre
    - Add JavaFX dependency
    - Move away from `javapackager` (dropped from JDK since 11).
    - Should be able to use [jarbundler](https://stackoverflow.com/questions/14917908/convert-java-to-app-file-for-macosx-using-jarbundler)
      on Mac.
- Reduce CPU usage
- Document database format
- Database version marker, for future format changes
- Allow files to be saved, viewed and downloaded

## Later
- Use haveibeenpwned.com API for breach alerts
- Change entry types
  - File: encrypted payload is bytes; base64 can be copypasta or saved to directory
  - URL: value can be opened in browser
- Persist random generator settings to local user file
- Closing database after inactivity moved out of front-end.

# Maybe
- Key-pair used to encrypt/decrypt file.
- Save-as
- Add gzip compression to database file
- Recent files drop-down on topbar
- Check for updates
    - <https://api.github.com/repos/limpygnome/parrot-manager/releases/latest>
    - May present privacy issues
- Multiple windows for viewing different databases
- Move from `es5` to `es6`

## Bugs
- none (known)
