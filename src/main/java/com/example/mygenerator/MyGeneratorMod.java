package com.example.mygenerator;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(MyGeneratorMod.MODID)
public class MyGeneratorMod {
    public static final String MODID = "mygenerator";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Block & Item
    public static final DeferredHolder<Block, Block> EMPTY_GENERATOR_BLOCK = BLOCKS.register("empty_generator",
            () -> new EmptyGeneratorBlock(BlockBehaviour.Properties.of().strength(2.0f)));

    public static final DeferredHolder<Item, Item> EMPTY_GENERATOR_ITEM = ITEMS.register("empty_generator",
            () -> new BlockItem(EMPTY_GENERATOR_BLOCK.get(), new Item.Properties()));

    // Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmptyGeneratorBlockEntity>> EMPTY_GENERATOR_BE =
            BLOCK_ENTITIES.register("empty_generator", () ->
                    BlockEntityType.Builder.of(EmptyGeneratorBlockEntity::new, EMPTY_GENERATOR_BLOCK.get()).build(null));

    // Menu
    public static final DeferredHolder<MenuType<?>, MenuType<EmptyGeneratorMenu>> EMPTY_GENERATOR_MENU =
            MENUS.register("empty_generator", () -> IMenuTypeExtension.create(EmptyGeneratorMenu::new));

    // Eigene Kreativ-Registerkarte (Creative Tab)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MY_GENERATOR_TAB = CREATIVE_MODE_TABS.register("generator_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("My Generator Mod"))
                    .icon(() -> new ItemStack(EMPTY_GENERATOR_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(EMPTY_GENERATOR_ITEM.get());
                    })
                    .build());

    public MyGeneratorMod(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Capability Event registrieren
        modEventBus.addListener(this::registerCapabilities);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::onRegisterScreens);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                EMPTY_GENERATOR_BE.get(),
                (blockEntity, side) -> blockEntity.getItemHandlerCapability(side)
        );
    }

    private void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(EMPTY_GENERATOR_MENU.get(), EmptyGeneratorScreen::new);
    }
}