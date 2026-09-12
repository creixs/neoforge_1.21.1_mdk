package com.example.mygenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class EmptyGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    private int progress = 0;
    private static final int MAX_PROGRESS = 20; // 20 Ticks = 1 Sekunde

    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public EmptyGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(MyGeneratorMod.EMPTY_GENERATOR_BE.get(), pos, state);
    }

    // Ticker-Methode wird jeden Tick (20x pro Sekunde) aufgerufen
    public static void tick(Level level, BlockPos pos, BlockState state, EmptyGeneratorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        ItemStack inputStack = blockEntity.itemHandler.getStackInSlot(0);

        // Nur arbeiten, wenn im Input-Slot ein Item liegt
        if (!inputStack.isEmpty()) {
            blockEntity.progress++;

            if (blockEntity.progress >= MAX_PROGRESS) {
                blockEntity.progress = 0;
                blockEntity.generateItem(inputStack);
            }
        } else {
            blockEntity.progress = 0;
        }
    }

    private void generateItem(ItemStack inputStack) {
        ItemStack outputStack = itemHandler.getStackInSlot(1);

        // Erstelle eine Kopie des Input-Items mit Anzahl 1
        ItemStack generated = inputStack.copyWithCount(1);

        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(1, generated);
        } else if (ItemStack.isSameItemSameComponents(outputStack, generated)
                && outputStack.getCount() < outputStack.getMaxStackSize()) {
            outputStack.grow(1);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Empty Generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EmptyGeneratorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        this.progress = tag.getInt("progress");
    }
}