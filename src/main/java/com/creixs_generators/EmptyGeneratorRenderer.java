package com.creixs_generators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EmptyGeneratorRenderer implements BlockEntityRenderer<EmptyGeneratorBlockEntity> {

    public EmptyGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EmptyGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // Das Ghost-Item aus Slot 0 holen
        ItemStack stack = blockEntity.itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();

        // 1. Position in der Mitte des Blocks ausrichten (X = 0.5, Y = 0.5, Z = 0.5)
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // 2. Langsame Rotation um die Y-Achse
        if (blockEntity.getLevel() != null) {
            long time = blockEntity.getLevel().getGameTime();
            float rotation = (time + partialTick) * 3.0F; // Geschwindigkeitsfaktor der Drehung
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        // 3. Etwas verkleinern, damit es perfekt in die Glasbox passt (z. B. 50% der Normalgröße)
        poseStack.scale(0.75F, 0.75F, 0.75F);

        // 4. Das Item rendern
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );

        poseStack.popPose();
    }
}