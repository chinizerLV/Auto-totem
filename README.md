# AutoTotem v2 — Fabric Mod (Minecraft 26.2)

Swaps a **Totem of Undying** into your offhand exactly **once** the
moment it pops - no continuous auto-changing, no repeated retries, and
it won't touch your inventory again after that single swap even if you
move items around. Press **Numpad 2** to toggle on/off.

## How this differs from the original AutoTotem

The original version continuously re-checked every couple ticks and
kept trying to swap a totem in for as long as the offhand was empty,
which could feel like it was "fighting" you if you were rearranging
your inventory manually.

This version instead:

1. Watches for the exact moment your offhand goes from "has a totem" to
   "empty" (the pop).
2. Waits a short fixed delay (2 ticks), then performs **exactly one**
   swap attempt.
3. After that single attempt, it does nothing further - even if you
   move items around - until your offhand actually has a totem in it
   again (which re-arms it to watch for the next pop).

## Building the jar (via GitHub Actions)

1. Push/upload this project to a GitHub repository.
2. Actions tab -> wait for green -> Artifacts -> `autototem-jar`.
3. Extract the downloaded zip for the real `.jar`.

## Installing

1. Fabric Loader for Minecraft **26.2**.
2. Fabric API for **26.2**.
3. Both jars into your `mods` folder.
4. Launch with the Fabric profile.

## Tuning

```java
private static final int DELAY_TICKS = 2;
```

Delay before the single swap attempt after a pop is detected.

## Toggle key

Numpad 2 - `GLFW.GLFW_KEY_KP_2` in `handleToggleKey()`.
