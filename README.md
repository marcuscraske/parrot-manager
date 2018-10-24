<h1 align="center">
    parrot manager
</h1>
<p align="center">
    <i>
        passwords made simple
    </i>
</p>

<p align="center">
    <img src="parrot-manager/src/main/resources/icons/parrot-icon.png" alt="parrot logo" />
</p>

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)
[![Release](https://img.shields.io/github/release/limpygnome/parrot-manager.svg)](https://github.com/limpygnome/parrot-manager/releases)
[![Downloads](https://img.shields.io/github/downloads/limpygnome/parrot-manager/total.svg)](https://github.com/limpygnome/parrot-manager/releases)
[![Linux and Mac Builds](https://img.shields.io/travis/limpygnome/parrot-manager.svg?label=Linux%20and%20Mac%20build)](https://travis-ci.org/limpygnome/parrot-manager)
[![Windows Build](https://img.shields.io/appveyor/ci/limpygnome/parrot-manager.svg?label=Windows%20build)](https://ci.appveyor.com/project/limpygnome/parrot-manager)
[![Github Issues](http://githubbadges.herokuapp.com/limpygnome/parrot-manager/issues.svg?style=flat-square)](https://github.com/limpygnome/parrot-manager/issues)
[![Pending Pull-Requests](https://img.shields.io/github/issues-pr/limpygnome/parrot-manager.svg)](https://github.com/limpygnome/parrot-manager/pulls)
[![Known Vulnerabilities](https://snyk.io/test/github/limpygnome/parrot-manager/badge.svg)](https://snyk.io/test/github/limpygnome/parrot-manager)

A simple password manager with a modern interface, with ability to synchronize databases using SSH.

Supports Linux, Windows and Mac.

<p align="center">
    <img src="docs/screenshots/7.0/parrot-window.png" alt="Parrot manager window" />
</p>
<p align="center">
    More screenshots can be found <a href="https://github.com/limpygnome/parrot-manager/tree/develop/docs/screenshots">here</a>.
</p>


## Features
- File encryption
- In-memory encryption (until revealed/copied/etc)
- Automatic backups on save
- Sync databases using SSH
    - Multiple hosts
    - Automatic merge to avoid conflicts and losing data
    - Works in hostile environments
- Copy values to clipboard
    - Automatically clear after period
- Send values as keys to other applications
- Import/export data
    - Currently supports csv and json
- Modern UI
- Standalone mode - useful for memory sticks and restrictive environments

Upcoming features can be seen on the [roadmap](roadmap.md).


## Download
Binaries are available on the [releases](https://github.com/limpygnome/parrot-manager/releases) page.


## Installation
Prerequisites:
- Recommended [Java Runtime 1.9](http://www.oracle.com/technetwork/java/javase/downloads/jre9-downloads-3848532.html).
    - Older versions have stability issues around webkit.

For help installing on your platform, see [installation](docs/installation.md).


## Contribute
This project is open to [contributions](CONTRIBUTING.md).
