# AutoTotem — Fabric Mod (Minecraft 26.2)

Automatically swaps a **Totem of Undying** into your offhand whenever
it's empty — after a **RANDOMISED DELAY**, not an instant robotic
reaction. Press **`NUM_PAD_2`** anytime to toggle it on/off — a chat
message confirms the state.

Built for **Minecraft 26.2**, using **Fabric Loader** + **Fabric API**,
with Java 25 and Minecraft's official (unobfuscated) mappings.

## How it actually works

Every client tick, the mod checks your offhand slot. The moment it's not
holding a Totem of Undying (e.g. you just used one, or never had one
equipped), it rolls a random delay — somewhere between 2 and 10 ticks
(0.1–0.5 seconds) — before pulling a totem from your inventory into the
offhand slot. It does this using the exact same click type the game
itself uses for the vanilla "swap to offhand" hotkey (F), so it's not
faking or bypassing anything server-side — it's just doing, automatically
and with human-like timing, what you'd otherwise do manually.

It's entirely client-side — no mixins, no packet spoofing, no
server-side changes needed.

Because it's client-side only, servers running anti-cheat (especially
PvP-focused ones) commonly detect and ban auto-totem behavior, since it
removes a meaningful skill/reaction element from combat. This is meant
for singleplayer or servers you control / have explicit permission to
use it on.

## Project layout

```
autototemmod/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── LICENSE
├── README.md
├── .github/workflows/build.yml      <- builds the jar automatically via GitHub Actions
└── src/
    ├── client/java/com/example/autototem/AutoTotemClient.java   <- all the logic lives here
    └── main/resources/fabric.mod.json
```

## Building the jar (via GitHub Actions — no local setup needed)

1. Push/upload this whole project to a GitHub repository.
2. GitHub Actions automatically builds it on every push (see
   `.github/workflows/build.yml`).
3. Go to the **Actions** tab on your repo → click the latest run → once
   it shows a green checkmark, scroll to **Artifacts** → download
   `autototem-jar`.
4. Extract the downloaded zip — inside is the real `autototem-1.0.0.jar`.

## Installing

1. Install **Fabric Loader** for Minecraft **26.2** from fabricmc.net.
2. Download **Fabric API** for **26.2** (Modrinth or CurseForge).
3. Put both the Fabric API jar and your built `autototem-*.jar` into your
   `mods` folder:
   - Windows: `%appdata%\.minecraft\mods`
   - Mac: `~/Library/Application Support/minecraft/mods`
4. Launch Minecraft using the **Fabric** profile.

## Using it

- Keep totems in your inventory (main inventory or hotbar — either
  works). No setup needed beyond that.
- Whenever your offhand totem is used or missing, it'll automatically
  restock after the randomised delay.
- Press `NUM_PAD_2` to toggle on/off.

## Tuning the delay

In `AutoTotemClient.java`:

```java
private static final int MIN_DELAY_TICKS = 2;
private static final int MAX_DELAY_TICKS = 8;
```

Both values are in ticks (20 ticks = 1 second). Widen or shift this
range to change how "human" the reaction timing looks — lower values
react faster but look more obviously automated, higher values are safer
but slower to restock. After editing: commit, wait for the GitHub
Actions build to go green, download the new jar, and swap it into your
`mods` folder in place of the old one.

## Toggle key

NUM_PAD_2 is hardcoded in `handleToggleKey()`. To use a different
key, swap `GLFW.GLFW_KEY_KP_2` for any other `GLFW_KEY_*` constant.
