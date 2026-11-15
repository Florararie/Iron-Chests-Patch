# Iron Chests Polymer Patch

A server-side [Polymer](https://modrinth.com/mod/polymer) patch for [Iron Chests](https://github.com/4nner/IronChests) 3.0.0 on Minecraft 26.1.2. Players without the mod, Polymer or a resource pack can see, place, open and upgrade the chests. Only the server needs anything installed.

## What it does

The chests stay real Iron Chests blocks, items and block entities on the server. Polymer sends each one to the client as a note block whose unused `custom_head` states select the tier and facing, and the bundled pack supplies a chest model and texture for every tier. Lock checks, upgrades, hand trucks, recipes and loot all run through the mod as before.

Items appear on the client as vanilla icons named for the tier, with capacity and traits in the lore. Upgrades and keys get matching stand-ins.

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

- No lid animation or chest sound for unmodded clients.
- The note block carrier gives chests a full-block collision shape and outline; the model is slightly inset.
- A note block tuned to the same `custom_head` states would also look like a chest.
- The crystal chest's contents are not visible.
- Locking works server-side. The key editor only opens for clients with Iron Core; others get a chat hint.
- Clients that install Iron Chests also see the virtualised chests.

Chest art comes from Iron Chests. This patch is MIT licensed; see `LICENSE`.
