# TODO / Roadmap

## Bugs
- Remote sync - when prompted for multiple database passwords, the second prompt does not have focus.

## Features
### Upcoming
- TBC

### Future
- prevent external requests entirely
- parent pom
- Move from `es5` to `es6`
- Setting to wipe clipboard within time period, after copying an entry.
  - Option to be toggled in settings
- Backups on remote host when syncing
- Save-as
- Implement generic key combos
  - ctrl+o for open
- Compress final build's artifacts
- Minification of resources and bundling only compiled assets
- Add gzip compression to database file
- Save random generator settings
- Make remote-sync async, with ability to cancel

### Ideas
- Import/export
  - Allow entire database to be exported in JSON or other compatible formats
  - Should be fairly easy for JSON and will be a gateway to other formats.

### Technical
- Upgrade Angular and npm dependencies
- Fix intl polyfill issues for JavaFx in order to use Angular date formatting
- Consider switching from JSON simple to Gson.
