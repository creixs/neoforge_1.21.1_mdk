# Creixs Generators

**Creixs Generators** is a fully functional, highly configurable, and NBT-persistent generator mod built for **Minecraft 1.21.1** using **NeoForge**.

Designed with Skyblock servers, technical gameplay, and server administrators in mind, it allows players to automate resources while giving admins complete control over balance and item permissions.

---

## Features

* **Dynamic Ghost-Slot Generator**: Place any allowed item/template into Slot 0 of the generator to start producing items into Slot 1.
* **Smart Dynamic GUI & NBT Persistence**: The generator block dynamically renames itself in inventory and menus based on the item inside (e.g., *Redstone Dust Generator*). When broken, output items drop to the ground while the template remains saved in the item's NBT!
* **Server-Side Configuration**: Tweak tick rates, output quantities, and auto-export behavior globally via `.minecraft/config/creixs_generators-common.toml`.
* **Flexible Whitelist System**: Support for both **Conventional Tags** (`c:ores`, `c:gems`, `c:dusts`, etc.) and specific item IDs (e.g., `minecraft:coal`, `minecraft:cobblestone`).
* **Automated Output**: Built-in auto-export functionality to directly push generated items into chests, hoppers, or pipes directly below the block.
* **Custom Rendering**: Floating and rotating item preview inside the generator's glass structure.

---

## Configuration (`creixs_generators-common.toml`)

Server administrators can fine-tune every aspect of the mod. Settings are managed server-side and take effect globally.

```toml
["Creixs Generators Settings"]
    # Duration in ticks required to produce 1 item (20 ticks = 1 second)
    generationTicks = 100

    # Amount of items produced per generation cycle
    outputAmount = 1

    # Whether the generator automatically pushes items into containers below
    enableAutoExport = true

["Whitelist Settings"]
    # List of Item Tags (Common/Conventional Tags) allowed as templates
    allowedTags = [
        "c:ores",
        "c:gems",
        "c:dusts",
        "c:raw_materials"
    ]

    # Specific item IDs allowed as templates
    allowedItems = [
        "minecraft:cobblestone",
        "minecraft:coal"
    ]