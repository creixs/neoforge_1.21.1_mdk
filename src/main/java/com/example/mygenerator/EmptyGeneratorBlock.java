package com.example.mygenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyGeneratorBlock extends Block implements EntityBlock {

    public EmptyGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new EmptyGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof EmptyGeneratorBlockEntity generator) {
                EmptyGeneratorBlockEntity.tick(lvl, pos, st, generator);
            }
        };
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmptyGeneratorBlockEntity generator) {
                // Interaktions-Test ohne GUI:
                // Rechtsklick mit einem Item setzt die Vorlage
                // Rechtsklick mit leerer Hand entnimmt das Output-Item
                ItemStack held = player.getMainHandItem();
                if (!held.isEmpty()) {
                    generator.getItemHandler().setStackInSlot(0, held.copyWithCount(1));
                } else {
                    ItemStack output = generator.getItemHandler().getStackInSlot(1);
                    if (!output.isEmpty()) {
                        player.getInventory().add(output.copy());
                        generator.getItemHandler().setStackInSlot(1, ItemStack.EMPTY);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}