# TODO / Roadmap

## Features
- Save-as
- Add ability to enable auto-backup / creates backed up versions.
  - Specify retention period / copies etc.
- Load last opened database on startup (with password prompt)
- Settings
  - Disable recent files
  - Automatic backups (enable/disable, max backups)
- Implement generic key combos
  - ctrl+s for saving
  - ctrl+o for open
- Implement right-click context menu of nodes.
  - Skeleton/POC is still sitting in WebViewStage.
- Import/export
  - Allow entire database to be exported in JSON or other compatible formats
  - Should be fairly easy for JSON and will be a gateway to other formats.
- Minification of resources and bundling only compiled assets

## Technical
- Consider switching from JSON simple to Gson.
- Fix intl polyfill issues for JavaFx in order to use Angular date formatting
