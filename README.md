# Iron Chests Polymer Patch

A server-side [Polymer](https://modrinth.com/mod/polymer) patch for [Iron Chests](https://github.com/4nner/IronChests) 3.0.0 on Minecraft 26.1.2. Players without the mod, Polymer or a resource pack can see, place, open and upgrade the chests. Only the server needs anything installed.

## What it does

Chests are a virtual display entity using the barrier block as a donor.

## Chest screens

Iron Chests' wide menu is replaced by a normal-height vanilla chest menu. A page is a whole number of rows: Christmas 3, copper 5, iron 6, and the larger chests are paged at 27, 36 and 42 slots. Shift-click, drag distribution and quick-move use the vanilla slot logic, so the dirt chest's filter still applies.

The navigation row is not a row of slot outlines. A font glyph paints the chest panel and border there instead, leaving only the prev/next buttons visible with the page number in their tooltips. The same glyph covers the unused cells at the end of a 42-slot page.

## Install

Server needs Java 25, Fabric Loader 0.19.5+, Fabric API, Polymer 0.16.x, Iron Chests 3.0.0 and Iron Core Utilities 1.0.0.

Clients need nothing. Polymer builds the pack from the jar's own assets; enable AutoHost (`config/polymer/auto-host.json`, `"enabled": true`) and restart, or run `/polymer generate-pack` and host the zip yourself. Without the pack the note blocks render as note blocks, and the chests still work.

## Building

```
gradle build
```

The jar lands in `build/libs/`. The patched mods come from CurseForge's maven; their file ids are in `gradle.properties`.

## Notes

- The crystal chest's contents are not visible.

Chest art comes from Iron Chests. This patch is MIT licensed; see `LICENSE`.
