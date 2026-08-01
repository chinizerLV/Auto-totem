package com.example.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * AutoTotem (v2 - single shot)
 * ----------------------------
 * Unlike the original version, this does NOT continuously watch and
 * retry every few ticks. Instead:
 *
 *   1. It tracks whether your offhand had a totem last tick.
 *   2. The MOMENT it detects a transition from "had totem" -> "empty"
 *      (i.e. the totem just popped), it waits a short fixed delay, then
 *      performs exactly ONE swap attempt from your inventory.
 *   3. After that one attempt (whether it succeeded or not), it does
 *      NOTHING further and will not touch your inventory again, even if
 *      you move items around, UNTIL your offhand has a totem in it
 *      again naturally (which re-arms the "watch for the next pop"
 *      state).
 *
 * This means: no repeated automatic swapping loop, no fighting with you
 * if you manually rearrange your inventory afterward - just one clean
 * reaction per pop.
 *
 * Press NUMPAD 2 to toggle the mod on/off.
 *
 * Written for Minecraft 26.2 (Fabric, unobfuscated official mappings).
 */
public class AutoTotemClient implements ClientModInitializer {

    // Fixed delay, in ticks (20 ticks = 1 second), before the single swap attempt.
    private static final int DELAY_TICKS = 2;

    private boolean enabled = true;
    private boolean lastToggleKeyState = false;

    // Was the offhand holding a totem last tick? Used to detect the "pop" edge.
    private boolean hadTotemLastTick = true;

    // True while we're mid-countdown for the single scheduled swap.
    private boolean swapScheduled = false;
    private int swapCooldown = 0;

    // True once we've already reacted to the current pop - blocks further
    // action until the offhand has a totem again (re-arming for next time).
    private boolean alreadyHandledThisPop = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        handleToggleKey(client);

        if (!enabled) {
            swapScheduled = false;
            return;
        }

        ItemStack offhand = client.player.getOffhandItem();
        boolean hasTotemNow = offhand.getItem() == Items.TOTEM_OF_UNDYING;

        if (hasTotemNow) {
            // Offhand has a totem again (either never popped, or a swap
            // succeeded, or you put one there yourself) - re-arm for the
            // next pop and clear any in-progress state.
            hadTotemLastTick = true;
            alreadyHandledThisPop = false;
            swapScheduled = false;
            return;
        }

        // Offhand does NOT have a totem right now.
        boolean justPopped = hadTotemLastTick && !hasTotemNow;
        hadTotemLastTick = false;

        if (justPopped && !alreadyHandledThisPop) {
            swapScheduled = true;
            swapCooldown = DELAY_TICKS;
        }

        if (swapScheduled) {
            swapCooldown--;
            if (swapCooldown <= 0) {
                int totemMenuSlot = findTotemMenuSlot(client);
                if (totemMenuSlot != -1) {
                    client.gameMode.handleContainerInput(0, totemMenuSlot, 40, ContainerInput.SWAP, client.player);
                }
                swapScheduled = false;
                alreadyHandledThisPop = true; // don't try again until offhand has a totem again
            }
        }
    }

    private int findTotemMenuSlot(Minecraft client) {
        var inventory = client.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return toMenuSlot(i);
            }
        }
        return -1;
    }

    private int toMenuSlot(int rawInventoryIndex) {
        if (rawInventoryIndex < 9) {
            return 36 + rawInventoryIndex;
        }
        return rawInventoryIndex;
    }

    private void handleToggleKey(Minecraft client) {
        long windowHandle = GLFW.glfwGetCurrentContext();
        boolean toggleKeyDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_KP_2) == GLFW.GLFW_PRESS;

        if (toggleKeyDown && !lastToggleKeyState) {
            enabled = !enabled;
            if (client.player != null) {
                client.player.sendSystemMessage(
                        Component.literal("AutoTotem: " + (enabled ? "ON" : "OFF"))
                );
            }
        }
        lastToggleKeyState = toggleKeyDown;
    }
}
