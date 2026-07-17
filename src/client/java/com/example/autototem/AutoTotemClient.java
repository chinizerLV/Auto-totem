package com.example.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoTotemClient implements ClientModInitializer {

    private static final int MIN_DELAY_TICKS = 2;
    private static final int MAX_DELAY_TICKS = 10;

    private boolean enabled = true;
    private boolean lastToggleKeyState = false;
    private int pendingSwapDelay = -1;
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
            pendingSwapDelay = -1;
            return;
        }

        if (pendingSwapDelay == -1) {
            pendingSwapDelay = MIN_DELAY_TICKS + random.nextInt(MAX_DELAY_TICKS - MIN_DELAY_TICKS + 1);
            return;
        }

        pendingSwapDelay--;
        if (pendingSwapDelay > 0) {
            return;
        }

        int totemMenuSlot = findTotemMenuSlot(client);
        if (totemMenuSlot != -1) {
            // TODO: re-enable once we find the correct ClickType location
        }
        pendingSwapDelay = -1;
    }

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
        if (rawInventoryIndex < 9) {
            return 36 + rawInventoryIndex;
        }
        return rawInventoryIndex;
    }

    private void handleToggleKey(Minecraft client) {
        long windowHandle = GLFW.glfwGetCurrentContext();
        boolean toggleKeyDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_BACKSLASH) == GLFW.GLFW_PRESS;

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
