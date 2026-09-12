package com.example.mygenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class EmptyGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    private int progress = 0;
    private static final int MAX_PROGRESS = 20; // 20 Ticks = 1 Sekunde

    // Internes Inventar (2 Slots)
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public EmptyGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(MyGeneratorMod.EMPTY_GENERATOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EmptyGeneratorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        ItemStack inputStack = blockEntity.itemHandler.getStackInSlot(0);

        // 1. Item-Generierung
        if (!inputStack.isEmpty()) {
            blockEntity.progress++;
            if (blockEntity.progress >= MAX_PROGRESS) {
                blockEntity.progress = 0;
                blockEntity.generateItem(inputStack);
            }
        } else {
            blockEntity.progress = 0;
        }

        // 2. Automatischer Export nach unten (Kisten, Drawer, Hopper, Pipes etc.)
        blockEntity.exportToBottomInventory();
    }

    private void generateItem(ItemStack inputStack) {
        ItemStack outputStack = itemHandler.getStackInSlot(1);
        ItemStack generated = inputStack.copyWithCount(1);

        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(1, generated);
        } else if (ItemStack.isSameItemSameComponents(outputStack, generated)
                && outputStack.getCount() < outputStack.getMaxStackSize()) {
            outputStack.grow(1);
        }
    }

    private void exportToBottomInventory() {
        ItemStack outputStack = itemHandler.getStackInSlot(1);
        if (outputStack.isEmpty() || level == null) return;

        BlockPos bottomPos = worldPosition.below();
        // Suche nach einem IItemHandler der darunterliegenden BlockEntity (Kiste, Hopper, Drawer, Pipe...)
        IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, bottomPos, Direction.UP);

        if (targetHandler != null) {
            // Versuche 1 Item aus Slot 1 unten einzufügen
            ItemStack singleItem = outputStack.copyWithCount(1);
            ItemStack remainder = ItemHandlerHelper.insertItem(targetHandler, singleItem, false);

            // Wenn das Einfügen erfolgreich war, reduzieren wir den Output-Slot
            if (remainder.isEmpty()) {
                outputStack.shrink(1);
                setChanged();
            }
        }
    }

    // Liefert nach außen hin (z. B. für Rohre/Pipes) nur den Output-Slot (Slot 1) als auslesbar zurück
    public IItemHandler getItemHandlerCapability(@Nullable Direction side) {
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return itemHandler.getStackInSlot(1);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                // Verhindert, dass externe Geräte Items von außen reinschieben
                return stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return itemHandler.extractItem(1, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(1);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }
        };
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