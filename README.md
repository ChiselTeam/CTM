# CTM (Connected Texture Mod)

CTM is a NeoForge library that provides advanced connected texture rendering for Minecraft blocks. It allows modders to easily implement various connection patterns and visual effects using a simple JSON-based model system and a fluent Data Generation API.

## Features

- **Multiple Connection Types**:
    - **Standard**: The classic 47-state connection pattern.
    - **TBS (Top-Bottom-Side)**: Specialized connections for blocks with distinct top/bottom faces.
    - **Directional (Horizontal/Vertical)**: Connections that follow a specific axis.
    - **Multiblock (2x2, 3x3, 4x4)**: Fixed or dynamic tiling patterns.
    - **Anti-Repeat (AR)**: Random rotation/tiling to break up repetitive patterns.
- **Eldritch Effect**: Dynamic UV-perturbation for mystical or shifting textures.
- **Fluent DataGen API**: Easily generate complex CTM model files.
- **NeoForge Native**: Built from the ground up for NeoForge 26.1.2.

## Installation

To use CTM in your development environment, add the following to your `build.gradle`:

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    compileOnly("maven.modrinth:ctmlib:${ctm_version}")
}
```

## Documentation

For detailed information on how to use the library, see the following guides:

- [**Data Generation Guide**](docs/DATAGEN.md) - Learn how to use the fluent API to generate models.

## Basic Usage

CTM models are defined in your blockstate JSON files using the `ctm:connected_texture_model` loader.

Example blockstate snippet:
```json
{
  "loader": "ctm:connected_texture_model",
  "model_location": "my_mod:block/my_block",
  "variant": {
    "block": "my_mod:my_block",
    "kind": "standard"
  }
}
```

## License

This project is licensed under the **GPL-3.0** license.
