# PvP Death Drops

A server-side Fabric mod for Minecraft 26.3. Players keep their inventory and XP on natural deaths — only PvP kills drop items.

## What it does

- **Natural death** (mobs, fire, fall, drowning, lava, void): your full inventory, XP levels, and score are restored on respawn.
- **PvP death** (killed by another player): items drop normally, just like vanilla.
- **Recent-hit rule**: if another player hit you within the last 15 seconds, a death right after counts as a PvP death — even if the killing blow was fall damage, drowning or fire. This is the fix for "my friend punched me, I fell, and I still kept my items".
- Respects the vanilla `keepInventory` gamerule — if that's on, everything is kept for everyone as normal.
- Works across all dimensions. Server-side only — no client mod needed.

## Install

Drop `pvpdeath-<version>.jar` into your server's `mods/` folder and restart.

## Configuration

On first start the mod writes `config/pvpdeath.json`:

```json
{
  "enabled": true,
  "keepItemsOnNaturalDeath": true,
  "keepXpOnNaturalDeath": true,
  "dropItemsOnPvp": true,
  "dropXpOnPvp": true,
  "recentPlayerHitCountsAsPvp": true,
  "recentPlayerHitWindowSeconds": 15.0,
  "debugLogging": false
}
```

| Option | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Master switch. `false` restores plain vanilla behaviour. |
| `keepItemsOnNaturalDeath` | `true` | Keep the inventory when nobody else is responsible for the death. |
| `keepXpOnNaturalDeath` | `true` | Keep XP/levels on a natural death. Independent of the items switch. |
| `dropItemsOnPvp` | `true` | Drop the inventory on a PvP death. Set `false` to keep it. |
| `dropXpOnPvp` | `true` | Drop XP on a PvP death. Set `false` to keep it. |
| `recentPlayerHitCountsAsPvp` | `true` | Treat a death inside the hit window as a PvP death. |
| `recentPlayerHitWindowSeconds` | `15.0` | Length of that window, in seconds. |
| `debugLogging` | `false` | Log why each death was classified the way it was. |

Items and XP are decided separately, so all four combinations work (keep items / drop XP, drop items / keep XP, etc.).

## Commands

- `/pvpdeath` — print the current settings (permission level 2 / gamemaster).
- `/pvpdeath reload` — re-read `config/pvpdeath.json` without restarting the server.

## Requirements

- Fabric Loader 0.19.5+
- Fabric API 0.161.0+
- Minecraft 26.3
- Java 25

## License

MIT
