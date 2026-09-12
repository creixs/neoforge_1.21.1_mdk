package com.example.mygenerator;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf.Logger;

@Mod(MyGeneratorMod.MOD_ID)
public class MyGeneratorMod {
    public static final String MOD_ID = "mygenerator";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Register-Instanzen für Blöcke, Items und BlockEntities
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    // 1. Block Registrierung
    public static final DeferredBlock<Block> EMPTY_GENERATOR_BLOCK = BLOCKS.register("empty_generator",
            () -> new EmptyGeneratorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    // 2. BlockItem Registrierung (damit der Block im Inventar gehalten/platziert werden kann)
    public static final DeferredItem<BlockItem> EMPTY_GENERATOR_ITEM = ITEMS.register("empty_generator",
            () -> new BlockItem(EMPTY_GENERATOR_BLOCK.get(), new Item.Properties()));

    // 3. BlockEntity Registrierung
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmptyGeneratorBlockEntity>> EMPTY_GENERATOR_BE =
            BLOCK_ENTITIES.register("empty_generator_be",
                    () -> BlockEntityType.Builder.of(EmptyGeneratorBlockEntity::new, EMPTY_GENERATOR_BLOCK.get()).build(null));

    public MyGeneratorMod(IEventBus modEventBus) {
        // Registrieren der Busse bei Mod-Start
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}