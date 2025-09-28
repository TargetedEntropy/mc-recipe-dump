# Recipe Dump - Minecraft Forge Mod

A simple Minecraft Forge mod that exports all registered recipes to a JSON file for analysis and documentation purposes.

## Features

- Dumps all registered Minecraft recipes to a formatted JSON file
- Supports all vanilla recipe types:
  - Shaped crafting recipes
  - Shapeless crafting recipes
  - Smelting recipes
  - Blasting recipes
  - Smoking recipes
  - Campfire cooking recipes
  - Stonecutting recipes
  - Smithing transform recipes
  - Smithing trim recipes
- Exports complete recipe data including ingredients, patterns, cooking times, and experience values
- Pretty-printed JSON output for easy reading

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.2.0 or higher
- Java 17 or higher

## Installation

1. Download the latest release JAR from the releases page
2. Place the JAR file in your Minecraft `mods` folder
3. Launch Minecraft with Forge

## Usage

Once in-game with operator permissions (level 2):

1. Open the chat/console
2. Type `/recipedump`
3. The mod will export all recipes to `recipes_dump.json` in your game directory
4. You'll receive a confirmation message with the file location and number of recipes exported

## Building from Source

### Prerequisites

- Java 17 or higher (Java 21 compatible)
- Git

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/mc-recipe-dump.git
   cd mc-recipe-dump
   ```

2. Build the mod:
   ```bash
   ./gradlew build
   ```
   On Windows:
   ```cmd
   gradlew.bat build
   ```

3. The built JAR will be located in `build/libs/`

### Development

To run the mod in a development environment:

- Client: `./gradlew runClient`
- Server: `./gradlew runServer`

## Output Format

The JSON output includes detailed information for each recipe:

```json
{
  "id": "minecraft:oak_planks",
  "type": "minecraft:crafting_shapeless",
  "group": "planks",
  "recipe_class": "shapeless",
  "ingredients": [...],
  "result": {
    "item": "minecraft:oak_planks",
    "count": 4
  }
}
```

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Support

If you encounter any issues or have questions, please open an issue on the GitHub repository.