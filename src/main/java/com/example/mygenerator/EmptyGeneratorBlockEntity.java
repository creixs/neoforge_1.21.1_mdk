package com.example.mygenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class EmptyGeneratorBlockEntity extends BlockEntity {

    // Slot 0 = Template (wird nicht verbraucht)
    // Slot 1 = Output (gesammelte Items)
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // Im Output Slot (1) darf manuell nichts abgelegt werden
            return slot == 0;
        }
    };

    private int timer = 0;

    public EmptyGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(MyGeneratorMod.EMPTY_GENERATOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EmptyGeneratorBlockEntity entity) {
        if (level == null || level.isClientSide) return;

        entity.timer++;
        if (entity.timer >= 20) { // Alle 20 Ticks (1 Sekunde)
            entity.timer = 0;
            entity.generateItem();
        }
    }

    private void generateItem() {
        ItemStack template = itemHandler.getStackInSlot(0);
        if (template.isEmpty()) return;

        ItemStack generatedStack = new ItemStack(template.getItem(), 1);

        // 1. Versand an ein Inventar direkt DARUNTER versuchen
        if (level != null) {
            IItemHandler targetHandler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    worldPosition.below(),
                    Direction.UP
            );

            if (targetHandler != null) {
                for (int i = 0; i < targetHandler.getSlots(); i++) {
                    generatedStack = targetHandler.insertItem(i, generatedStack, false);
                    if (generatedStack.isEmpty()) {
                        return; // Vollständig im Unter-Inventar abgelegt
                    }
                }
            }
        }

        // 2. Falls unten kein Platz war oder kein Inventar da ist -> In internen Output-Slot (Slot 1)
        ItemStack currentOutput = itemHandler.getStackInSlot(1);
        if (currentOutput.isEmpty()) {
            itemHandler.setStackInSlot(1, generatedStack);
        } else if (ItemStack.isSameItemSameComponents(currentOutput, generatedStack)
                && currentOutput.getCount() < currentOutput.getMaxStackSize()) {
            currentOutput.grow(1);
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
    }
}