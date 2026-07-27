# AutoTotem — Fabric Mod (Minecraft 26.2)

Automatically swaps a **Totem of Undying** into your offhand whenever
it's empty — after a **fixed 5-tick delay** (0.25 seconds), instead of
an instant robotic reaction. Press **`\`** (backslash) anytime to toggle
it on/off — a chat message confirms the state.

## How it actually works

Every client tick, the mod checks your offhand slot. The moment it's not
holding a Totem of Undying, it waits a fixed 5 ticks before pulling a
totem from your inventory into the offhand slot, using the same "input
type" the game uses for the vanilla swap-to-offhand hotkey (F).

**A real limitation worth knowing:** when the mod swaps in a totem, your
screen updates immediately, but the server needs a moment to confirm it.
If you take lethal damage in that narrow window — especially with any
noticeable ping — you can die despite what your screen showed. No
client-side mod can fully eliminate this; it's a networking-latency
issue, not a logic bug.

## Tuning the delay

```java
private static final int DELAY_TICKS = 5;
```

Lower = faster reaction (less risk of the death-desync issue, but more
robotic-looking). Higher = slower, more human-looking.
