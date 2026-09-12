package com.creixs_generators;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

public class EmptyGeneratorBlockItem extends BlockItem {

    public EmptyGeneratorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        // Liest die NBT-Daten der BlockEntity aus den DataComponents
        CustomData customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);

        if (!customData.isEmpty()) {
            var tag = customData.copyTag();
            if (tag.contains("inventory")) {
                var invTag = tag.getCompound("inventory");
                if (invTag.contains("Items")) {
                    var itemsList = invTag.getList("Items", 10);
                    for (int i = 0; i < itemsList.size(); i++) {
                        var itemTag = itemsList.getCompound(i);

                        // Überprüfe Slot 0 (Ghost-Slot)
                        if (itemTag.getByte("Slot") == 0) {
                            // In 1.21.1 speichert Minecraft die Item-ID unter dem Key "id"
                            if (itemTag.contains("id")) {
                                String itemId = itemTag.getString("id");
                                ResourceLocation location = ResourceLocation.tryParse(itemId);

                                if (location != null) {
                                    Item item = BuiltInRegistries.ITEM.get(location);
                                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                                        // Erstelle einen temporären Stack für den sauberen Namen
                                        ItemStack templateStack = new ItemStack(item);
                                        return Component.literal(templateStack.getHoverName().getString() + " Generator");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.getName(stack);
    }
}