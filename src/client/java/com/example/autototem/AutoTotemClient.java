package com.example.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * AutoTotem
 * ---------
 * Watches your offhand slot. The instant it's not holding a Totem of
 * Undying, this schedules a swap-in from your inventory after a
 * RANDOMISED delay (instead of an instant, robotic 0-tick reaction),
 * then performs the swap via the same click type the game uses for the
 * vanilla "swap to offhand" hotkey.
 *
 * Press BACKSLASH ( \ ) to toggle on/off.
 *
 * Written for Minecraft 26.2 (Fabric, unobfuscated official mappings).
 */
public class AutoTotemClient implements ClientModInitializer {

    // Randomised delay window, in ticks (20 ticks = 1 second).
    private static final int MIN_DELAY_TICKS = 2;
    private static final int MAX_DELAY_TICKS = 10;

    private boolean enabled = true;
    private boolean lastToggleKeyState = false;
    private int pendingSwapDelay = -1; // -1 means "no swap currently scheduled"
    private final Random random = new Random();

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
            pendingSwapDelay = -1;
            return;
        }

        ItemStack offhand = client.player.getOffhandItem();
        boolean needsTotem = offhand.getItem() != Items.TOTEM_OF_UNDYING;

        if (!needsTotem) {
            // Offhand already has a totem, nothing to do, cancel any pending timer.
            pendingSwapDelay = -1;
            return;
        }

        // First tick we notice the offhand is empty -> roll a random delay.
        if (pendingSwapDelay == -1) {
            pendingSwapDelay = MIN_DELAY_TICKS + random.nextInt(MAX_DELAY_TICKS - MIN_DELAY_TICKS + 1);
            return;
        }

        pendingSwapDelay--;
        if (pendingSwapDelay > 0) {
            return; // still waiting out the randomised delay
        }

        int totemMenuSlot = findTotemMenuSlot(client);
        if (totemMenuSlot != -1) {
            // Same click type the vanilla "swap to offhand" key (F) uses.
            client.gameMode.handleInventoryMouseClick(0, totemMenuSlot, 40, ClickType.SWAP, client.player);
        }
        pendingSwapDelay = -1;
    }

    /**
     * Finds a Totem of Undying in the player's main inventory/hotbar and
     * returns its slot number using the player's inventory MENU numbering
     * (not the raw internal inventory index) since that's what the click
     * packet expects.
     */
    private int findTotemMenuSlot(Minecraft client) {
        var inventory = client.player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (inventory.items.get(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return toMenuSlot(i);
            }
        }
        return -1;
    }

    private int toMenuSlot(int rawInventoryIndex) {
        // Raw inventory indices 0-8 are the hotbar, which map to menu
        // slots 36-44. Indices 9-35 (main inventory) map 1:1 to the same
        // menu slot numbers.
        if (rawInventoryIndex < 9) {
            return 36 + rawInventoryIndex;
        }
        return rawInventoryIndex;
    }

    private void handleToggleKey(Minecraft client) {
        long windowHandle = GLFW.glfwGetCurrentContext();
        boolean toggleKeyDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_F6) == GLFW.GLFW_PRESS;

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
