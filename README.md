# One Block Curse

A Minecraft Fabric mod that adds a mysterious curse mechanic to your world. When you create a new world, one random common block type becomes secretly cursed - and breaking it may trigger unpredictable effects!

## Features

### The Curse Mechanic
- **Random Selection**: Each world has ONE randomly selected cursed block type chosen from common blocks: Stone, Dirt, Sand, Gravel, Oak Log, Cobblestone, Grass Block, Granite, Diorite, Andesite, Clay, and Netherrack
- **World Persistence**: The cursed block type stays the same for the entire world - it's saved and persists across server restarts
- **15% Trigger Chance**: Every time you break a cursed block, there's a 15% chance something will happen

### Curse Effects
When triggered, one of five random effects occurs:

1. **Small Explosion** - A TNT-sized explosion at the block location
2. **Hostile Mob Spawn** - 3 hostile mobs spawn nearby (Zombie, Skeleton, Spider, Creeper, or Enderman)
3. **Positive Buff** - You receive a random positive effect for 30 seconds (Speed, Strength, Jump Boost, Regeneration, Resistance, Haste, Night Vision, or Fire Resistance) at level 2
4. **Bonus Diamonds** - 2-4 diamonds drop from the block
5. **Reverse Gravity** - Levitation effect for 5 seconds (with Slow Falling to prevent fall damage)

### Commands
- `/cursedblock` - Reveals which block type is cursed in your current world (available to all players)

## How to Use

1. Install the mod and start a new world
2. The curse is automatically assigned to a random common block
3. Mine as usual - you won't know which block is cursed until you trigger an effect or use the command
4. For a challenge, try to survive without using `/cursedblock` to discover which block is cursed!

## Gameplay Tips

- The curse affects everyone on the server equally - coordinate with friends!
- If you suspect a block is cursed, mine carefully and be prepared for explosions or mobs
- The buff and diamond effects make the curse a risk-reward mechanic
- In multiplayer, keep the cursed block secret from other players for added suspense

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.0 or higher
- Fabric API

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your mods folder
3. Download the `one-block-curse-1.0.0.jar` from the releases
4. Place the jar file in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Building from Source

```bash
git clone https://github.com/Simplifine-gamedev/one-block-curse.git
cd one-block-curse
./gradlew build
```

The built jar will be in `build/libs/`.

## License

This mod is provided as-is for personal use and server deployment.

## Author

Created by ali77sina
