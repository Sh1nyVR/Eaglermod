# Eaglermod

Eaglermod is an in-client mod system for EaglercraftX 1.8 designed around portable `.eaglmod` packages and a Minecraft-native visual Mod Builder.

## `.eaglmod` v1

`.eaglmod` is a compact binary container rather than a renamed ZIP.

```text
8 bytes   EAGLMOD1
int32     manifest JSON byte length
bytes     UTF-8 manifest JSON
int32     embedded file count
repeat:
  uint16  UTF-8 path length
  bytes   path
  int32   data length
  bytes   data
```

The manifest contains `id`, `name`, `version`, `author`, `description`, `website`, `entrypoint`, enabled state and tags.

Builder mods store their visual program at `project/blocks.json`. Assets live under `assets/`, for example:

```text
assets/images/logo.png
assets/sounds/boom.ogg
assets/models/example.json
assets/data/config.json
```

Installed packages are persisted in Eaglercraft's VFS under `eaglermod/mods`, which maps to the browser-backed Eagler filesystem instead of the small LocalStorage quota.

## Mods screen

The title screen exposes a **Mods** button. The Mods screen shows installed mod name, version, author, description and enabled state. It can import, export and toggle packages and launch Mod Builder.

## Mod Builder

The editor is intentionally rendered with Minecraft's GUI/font APIs. It has Code, Assets and Mod Info tabs.

The Code workspace uses Scratch-style puzzle blocks. A new project begins with a `when TEST` event connected to a chat action. Palette actions currently include:

- `when TEST`
- `when TICK`
- chat text
- title/message text
- Minecraft command
- FOV
- gamma

The block graph is data, not generated Java source, so projects remain portable across browser builds. `Test` installs the current package in-memory/persistent storage, reloads the runtime and fires the TEST event. `Build` installs the package and downloads a `.eaglmod` file.

## Assets

The first builder supports direct import of PNG images, OGG sounds, JSON models and JSON data. The package format itself is binary-safe, so more asset types can be added without changing the container.

## Runtime direction

The v1 runtime is deliberately capability-based. Builder opcodes are handled by Eaglermod rather than executing unrestricted arbitrary JavaScript. This keeps visual mods predictable and gives Eaglermod a stable API surface. A later JS-friendly EaglScript layer can compile into the same runtime actions/events while preserving `.eaglmod` compatibility.

Planned event/API expansion includes game tick, world join/leave, chat receive/send, key input, render HUD/world, entity/block interaction, custom settings screens, custom tabs, textures, sounds, models, recipes and multiplayer-safe networking hooks.
