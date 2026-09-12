package com.creixs_generators;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

// Eigener Slot für Ghost / Phantom Items
public class GhostSlot extends SlotItemHandler {

    public GhostSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        // Spieler kann das Phantom-Item nicht normal mit der Maus herausnehmen
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Verhindert das normale Ablegen
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
