package com.example.mygenerator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EmptyGeneratorMenu extends AbstractContainerMenu {

    private final EmptyGeneratorBlockEntity blockEntity;

    // Client-Konstruktor
    public EmptyGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Server-Konstruktor
    public EmptyGeneratorMenu(int containerId, Inventory playerInventory, BlockEntity entity) {
        super(MyGeneratorMod.EMPTY_GENERATOR_MENU.get(), containerId);
        this.blockEntity = (EmptyGeneratorBlockEntity) entity;

        // Slot 0: Input (links)
        // Slot 0 ist jetzt unser GhostSlot!
        this.addSlot(new GhostSlot(blockEntity.itemHandler, 0, 56, 35));
        // Slot 1: Output (rechts)
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 1, 116, 35));

        // Spieler-Inventar hinzufügen (3x9 Hauptinventar + 9 Hotbar)
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
                net.minecraft.world.inventory.ContainerLevelAccess.create(
                        this.blockEntity.getLevel(),
                        this.blockEntity.getBlockPos()
                ),
                player,
                MyGeneratorMod.EMPTY_GENERATOR_BLOCK.get()
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            // Wenn aus Output-Slot (1) geholt wird -> ins Spieler-Inventar
            if (index == 1) {
                if (!this.moveItemStackTo(stackInSlot, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, itemstack);
            }
            // Wenn aus Spieler-Inventar geklickt wird -> Ghost-Item in Slot 0 setzen
            else if (index >= 2) {
                this.blockEntity.itemHandler.setStackInSlot(0, stackInSlot.copyWithCount(1));
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Prüfen, ob der geklickte Slot der Ghost-Slot (Slot 0) ist
        if (slotId == 0) {
            ItemStack carried = getCarried();

            if (carried.isEmpty()) {
                // Wenn die Hand leer ist: Ghost-Item löschen (Slot leeren)
                this.blockEntity.itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            } else {
                // Wenn der Spieler ein Item hält: Phantom-Kopie mit Anzahl 1 im Ghost-Slot ablegen
                ItemStack ghostStack = carried.copyWithCount(1);
                this.blockEntity.itemHandler.setStackInSlot(0, ghostStack);
            }
            return; // Normales Inventar-Handling für diesen Slot überspringen
        }

        super.clicked(slotId, button, clickType, player);
    }
}