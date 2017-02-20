# parrot library
This module contains most of the core functionality of parrot, which is areas such as cryptography and the (password)
database.

Classes should use basic design patterns and not rely on dependency injection, with a high level of scrutiny in
unit testing.

It should be possible to re-use this library, but the current intention is just to keep the front-end entirely
separate and disposable.
