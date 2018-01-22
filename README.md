<h1 align="center">
    parrot manager
</h1>
<p align="center">
    <i>passwords made simple</i>
</p>

<p align="center">
    <img src="parrot-manager/src/main/resources/icons/parrot-icon.png" alt="parrot logo" />
</p>

[![Build Status](https://travis-ci.org/limpygnome/parrot-manager.svg)](https://travis-ci.org/limpygnome/parrot-manager)
[![Github All Releases](https://img.shields.io/github/downloads/atom/atom/total.svg)](https://github.com/limpygnome/parrot-manager)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)
[![Github Issues](http://githubbadges.herokuapp.com/limpygnome/parrot-manager/issues.svg?style=flat-square)](https://github.com/limpygnome/parrot-manager/issues)
[![Pending Pull-Requests](http://githubbadges.herokuapp.com/limpygnome/parrot-manager/pulls.svg?style=flat-square)](https://github.com/limpygnome/parrot-manager/pulls)

A simple password manager with a modern interface, with ability to synchronize databases using SSH.

Supports Linux, Windows and Mac.

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

Upcoming features can be seen on the [roadmap](roadmap.md).


## Download
Binaries are available on the [releases](https://github.com/limpygnome/parrot-manager/releases) page.


## Screenshots
<table>
    <tr>
        <td>
            <img src="media/screenshot-mac-menu.png" alt="open menu on mac" />
        </td>
        <td>
            <img src="media/screenshot-mac-installer.png" alt="installer for mac" />
        </td>
    </tr>
    <tr>
        <td>
            <img src="media/screenshot-list.png" alt="listing of entries" />
        </td>
        <td>
            <img src="media/screenshot-entry.png" alt="viewing an entry and history" />
        </td>
    </tr>
    <tr>
        <td>
            <img src="media/screenshot-backups.png" alt="database backups" />
        </td>
        <td>
            <img src="media/screenshot-sync.png" alt="database backups" />
        </td>
    </tr>
    <tr>
        <td>
            <img src="media/screenshot-sync-2.png" alt="database backups" />
        </td>
        <td>
            <img src="media/screenshot-password.png" alt="entering password with hacker theme" />
        </td>
    </tr>
    <tr>
        <td>
            <img src="media/screenshot-hacker.png" alt="hacker theme" />
        </td>
        <td>
            <img src="media/screenshot-dark.png" alt="dark theme" />
        </td>
    </tr>
    <tr>
        <td>
            <img src="media/screenshot-settings.png" alt="settings with dark theme" />
        </td>
        <td>
            <img src="media/screenshot-windows-installer.png" alt="windows installer" />
        </td>
    </tr>
</table>


## Installation
### Prerequisites
- Recommended [Java Runtime 1.9](http://www.oracle.com/technetwork/java/javase/downloads/jre9-downloads-3848532.html)
    - Minimum [Java Runtime 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html), however
      there are various JRE bugs that may cause stability issues.

### Ubuntu / Debian
Download the latest deb file, to anywhere, and run the following command in the same directory; the file name
may differ:

````
sudo dpkg -i parrot-manager.deb
````

### Windows
Both an installer and stand-alone version are available.

For the stand-alone, just extract the executable to anywhere you desire, no installation process is required.

### Mac
Download the latest pkg file, to anywhere, and

### Other Platforms
Download the latest jar zip or tar archive, extract the jar file and run:

````
java -jar parrot-manager.jar
````

File name may differ and no installation process is required, as the JAR is standalone.


## Dependencies

[![Dependency Status](https://gemnasium.com/badges/github.com/limpygnome/parrot-manager.svg)](https://gemnasium.com/github.com/limpygnome/parrot-manager)
[![Dependency Status](https://www.versioneye.com/user/projects/58f5b1d4307d03003e9de24e/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58f5b1d4307d03003e9de24e)

## Contribute
This project is open to [contributions](contribute.md).

Donations can be sent to:
* PayPal: limpygnome@gmail.com
* Bitcoin: 1CwSZwBT5qVbuKABUj2ruz5VyHhY23E6bY
