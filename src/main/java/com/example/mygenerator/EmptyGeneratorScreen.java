package com.example.mygenerator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MyGeneratorScreen extends AbstractContainerScreen<MyGeneratorMenu> {

    // Pfad zu deinem erstellten PNG
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyGeneratorMod.MODID, "textures/gui/container/generator_gui.png");

    public MyGeneratorScreen(MyGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;  // Breite deines Fensters
        this.imageHeight = 166; // Höhe deines Fensters
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Zeichnet deine 256x256 Textur ab Position (0,0) des Fensters
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}