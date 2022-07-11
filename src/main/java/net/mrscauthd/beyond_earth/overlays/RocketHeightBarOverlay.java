package net.mrscauthd.beyond_earth.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.mrscauthd.beyond_earth.BeyondEarth;
import net.mrscauthd.beyond_earth.entities.LanderEntity;
import net.mrscauthd.beyond_earth.events.Methods;
import net.mrscauthd.beyond_earth.events.forge.PlanetOverlayEvent;
import net.mrscauthd.beyond_earth.guis.helper.ScreenHelper;
import net.mrscauthd.beyond_earth.registries.LevelRegistry;

public class RocketHeightBarOverlay implements IGuiOverlay {

    /** PLANET BAR TEXTURES */
    public static final ResourceLocation MOON_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/moon_planet_bar.png");
    public static final ResourceLocation MARS_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/mars_planet_bar.png");
    public static final ResourceLocation MERCURY_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/mercury_planet_bar.png");
    public static final ResourceLocation VENUS_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/venus_planet_bar.png");
    public static final ResourceLocation GLACIO_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/glacio_planet_bar.png");
    public static final ResourceLocation EARTH_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/earth_planet_bar.png");
    public static final ResourceLocation ORBIT_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/orbit_planet_bar.png");

    /** ROCKET TEXTURE */
    public static final ResourceLocation ROCKET_PLANET_BAR_TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/planet_bar/rocket.png");

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Player player = Minecraft.getInstance().player;

        if (Methods.isRocket(player.getVehicle()) || player.getVehicle() instanceof LanderEntity) {
            Level level = Minecraft.getInstance().level;

            float yHeight = (float) player.getY() / 5.3F;

            if (yHeight < 0) {
                yHeight = 0;
            } else if (yHeight > 113) {
                yHeight = 113;
            }

            ResourceLocation planet;

            if (Methods.isLevel(level, LevelRegistry.MOON)) {
                planet = MOON_PLANET_BAR_TEXTURE;
            } else if (Methods.isLevel(level, LevelRegistry.MARS)) {
                planet = MARS_PLANET_BAR_TEXTURE;
            } else if (Methods.isLevel(level, LevelRegistry.MERCURY)) {
                planet = MERCURY_PLANET_BAR_TEXTURE;
            } else if (Methods.isLevel(level, LevelRegistry.VENUS)) {
                planet = VENUS_PLANET_BAR_TEXTURE;
            } else if (Methods.isLevel(level, LevelRegistry.GLACIO)) {
                planet = GLACIO_PLANET_BAR_TEXTURE;
            } else if (Methods.isOrbitLevel(level)) {
                planet = ORBIT_PLANET_BAR_TEXTURE;
            } else {
                planet = EARTH_PLANET_BAR_TEXTURE;
            }

            PlanetOverlayEvent event = new PlanetOverlayEvent(gui, planet, poseStack, partialTick, width, height);
            MinecraftForge.EVENT_BUS.post(event);

            if (planet != event.getResourceLocation()) {
                planet = event.getResourceLocation();
            }

            /** ROCKET BAR IMAGE */
            RenderSystem.setShaderTexture(0, planet);
            gui.blit(poseStack, 0, (height / 2) - 128 / 2, 0, 0, 16, 128, 16, 128);

            /** ROCKET_Y IMAGE */
            RenderSystem.setShaderTexture(0, ROCKET_PLANET_BAR_TEXTURE);
            ScreenHelper.renderWithFloat.blit(poseStack, 4, (height / 2) + (103 / 2) - yHeight, 0, 0, 8, 11, 8, 11);
        }
    }
}