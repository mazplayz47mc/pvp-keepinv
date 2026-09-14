# PvP Death Drops

A server-side Fabric mod for Minecraft 26.2. Players keep their inventory and XP on natural deaths — only PvP kills drop items.

## What it does

- **Natural death** (mobs, fire, fall, drowning, lava, void): your full inventory, XP levels, and score are restored on respawn.
- **PvP death** (killed by another player): items drop normally, just like vanilla.
- Respects the vanilla `keepInventory` gamerule — if that's on, everything is kept for everyone as normal.
- Works across all dimensions. Server-side only — no client mod needed.

## Install

Drop `pvpdeath-<version>.jar` into your server's `mods/` folder and restart.

## Requirements

- Fabric Loader 0.19.5+
- Fabric API 0.160.0+
- Minecraft 26.2
- Java 25

## License

MIT