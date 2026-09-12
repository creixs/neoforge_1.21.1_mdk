package com.creixs_generators;

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
        super(CreixsGeneratorsMod.EMPTY_GENERATOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EmptyGeneratorBlockEntity entity) {
        if (level.isClientSide()) return;

        ItemStack template = entity.itemHandler.getStackInSlot(0);

        // Prüfe, ob ein Item drin liegt UND ob es gewhitelistet ist
        if (template.isEmpty() || !GeneratorConfig.isItemAllowed(template)) {
            return;
        }

        entity.progress++;

        // Generierungs-Logik
        if (entity.progress >= GeneratorConfig.GENERATION_TICKS.get()) {
            entity.progress = 0;

            ItemStack result = template.copy();
            result.setCount(GeneratorConfig.OUTPUT_AMOUNT.get());

            ItemStack currentOutput = entity.itemHandler.getStackInSlot(1);
            if (currentOutput.isEmpty()) {
                entity.itemHandler.setStackInSlot(1, result);
                entity.setChanged();
            } else if (ItemStack.isSameItemSameComponents(currentOutput, result) &&
                    currentOutput.getCount() + result.getCount() <= currentOutput.getMaxStackSize()) {
                currentOutput.grow(result.getCount());
                entity.setChanged();
            }
        }

        if (GeneratorConfig.ENABLE_AUTO_EXPORT.get()) {
            entity.exportToBottomTile();
        }
    }

    private void exportToBottomTile() {
        if (level == null || level.isClientSide()) return;

        ItemStack outputStack = this.itemHandler.getStackInSlot(1);
        if (outputStack.isEmpty()) return;

        // Das BlockEntity direkt unter dem Generator holen
        BlockPos belowPos = this.worldPosition.below();

        // NeoForge ItemHandler-Capability unterhalb abfragen
        var cap = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, belowPos, net.minecraft.core.Direction.UP);
        if (cap != null) {
            for (int slot = 0; slot < cap.getSlots(); slot++) {
                // Versuche 1 Item einzufügen
                ItemStack singleItem = outputStack.copyWithCount(1);
                ItemStack remainder = cap.insertItem(slot, singleItem, false);

                if (remainder.isEmpty()) {
                    // Bei Erfolg: 1 Item aus dem Generator-Output entfernen
                    outputStack.shrink(1);
                    this.setChanged();
                    break;
                }
            }
        }
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
        // 1. Ghost-Item aus Slot 0 abfragen
        ItemStack template = this.itemHandler.getStackInSlot(0);

        // 2. Wenn ein Template gesetzt ist, dynamischen Namen erzeugen
        if (!template.isEmpty()) {
            return Component.literal(template.getHoverName().getString() + " Generator");
        }

        // 3. Wenn leer, Standard-Titel nutzen
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
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

    // 1. Diese NBT-Übertragungsmethoden für Client-Sync ergänzen/anpassen:
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    // 2. In onItemHandlerContentsChanged (bzw. setChanged) ein Block-Update anfordern:
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}