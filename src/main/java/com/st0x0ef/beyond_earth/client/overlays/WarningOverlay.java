package com.st0x0ef.beyond_earth.client.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import com.st0x0ef.beyond_earth.BeyondEarth;
import com.st0x0ef.beyond_earth.common.entities.LanderEntity;

public class WarningOverlay implements IGuiOverlay {

    public static final ResourceLocation WARNING = new ResourceLocation(BeyondEarth.MODID, "textures/overlay/warning.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Entity vehicle = player.getVehicle();

        if (player.getVehicle() instanceof LanderEntity && !player.getVehicle().isInWall() && !player.isInFluidType()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            /** FLASHING */
            float sin = (float) Math.sin((mc.level.getDayTime() + partialTick) / 6.0f);
            float flash = Mth.clamp(sin, 0.0f, 4.0f);

            RenderSystem.setShaderColor(flash, flash, flash, flash);

            /** WARNING IMAGE */
            RenderSystem.setShaderTexture(0, WARNING);
            graphics.blit(WARNING, width / 2 - 58, 50, 0, 0, 116, 21, 116, 21);

            /** SPEED TEXT */
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            double speed = Math.round(100.0 * (vehicle).getDeltaMovement().y()) / 100.0;

            Component message = Component.translatable("message." + BeyondEarth.MODID + ".speed", speed);
            graphics.drawString(Minecraft.getInstance().font, message, width / 2 - 29, 80, -3407872);
        }
    }
}
