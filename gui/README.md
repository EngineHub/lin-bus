lin-bus NBT editor
==================

This is a JavaFX-based editor for NBT files. It serves as an example of what you can do with this API, as well as a
modern editor for NBT files. It is not yet feature-complete on the editing side, but it works great to view files.

## TODO

- Add/remove entries
- Undo/redo changes
- Refine editing UX/design
- Right-click menus for easier access to common actions

## Building

Building is as simple as `./gradlew build`. Note that if you want to make the packages, you should the `jpackage` task,
and you will need to have `rpmbuild` to make the RPM package, and `dpkg-deb` to make the Debian package. Other
dependencies may be necessary for other package types. Packages can also only be built on the OS they are intended for,
due to a `jpackage` limitation.
