# Reality Shift

A showcase `.eaglmod` for the Eaglermod v0.1 runtime.

## TEST

Select **Reality Shift** in the Mods screen and press **Test**. It will:

- play a cave sound
- change world time to midnight
- force rain and thunder visually
- turn gamma up to 10
- change FOV to 120
- switch to front third-person camera
- disable view bobbing
- print styled status messages

## RESET

Select the mod and press **Reset**. Its RESET event restores daytime, clear weather, gamma 0, FOV 70, first-person camera and view bobbing.

The editable block graph is in `project.json`. The downloadable package contains the same graph as `project/blocks.json` plus a small example data asset under `assets/data/`.
