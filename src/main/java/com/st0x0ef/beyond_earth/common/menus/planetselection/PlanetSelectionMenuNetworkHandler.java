package com.st0x0ef.beyond_earth.common.menus.planetselection;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import com.st0x0ef.beyond_earth.common.menus.planetselection.helper.PlanetSelectionMenuNetworkHandlerHelper;
import com.st0x0ef.beyond_earth.common.util.Methods;
import com.st0x0ef.beyond_earth.common.util.Planets;

public class PlanetSelectionMenuNetworkHandler extends PlanetSelectionMenuNetworkHandlerHelper {
    public int integer;

    public PlanetSelectionMenuNetworkHandler(int integer) {
        this.integer = integer;
    }

    public PlanetSelectionMenuNetworkHandler(FriendlyByteBuf buffer) {
        this.integer = buffer.readInt();
    }

    public static PlanetSelectionMenuNetworkHandler decode(FriendlyByteBuf buffer) {
        return new PlanetSelectionMenuNetworkHandler(buffer);
    }

    public static void encode(PlanetSelectionMenuNetworkHandler message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.integer);
    }

    public static void handle(PlanetSelectionMenuNetworkHandler message,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            boolean isPlanet = Planets.PLANET_ID_MAPS.containsKey(message.integer);
            if (isPlanet) {
                ResourceKey<Level> dest = Planets.PLANET_ID_MAPS.get(message.integer);
                message.defaultOptions(player);
                Methods.createLanderAndTeleportTo(player, dest, 700, false);
                return;
            }

            boolean isOrbit = Planets.ORBIT_ID_MAPS.containsKey(message.integer);
            if (isOrbit) {
                ResourceKey<Level> dest = Planets.ORBIT_ID_MAPS.get(message.integer);
                message.defaultOptions(player);
                Methods.createLanderAndTeleportTo(player, dest, 700, false);
                return;
            }

            boolean isStation = Planets.STATION_ID_MAPS.containsKey(message.integer);
            if (isStation) {
                ResourceKey<Level> dest = Planets.STATION_ID_MAPS.get(message.integer);
                if(Methods.canPlaceStation(player.getServer().getLevel(dest), player)) {
                    message.defaultOptions(player);
                    message.deleteItems(player);
                    Methods.createLanderAndTeleportTo(player, dest, 700, true);
                } else {
                    //TODO : ADD SOMETHING TO TELL THE PLAYER HE CAN'T PLACE SPACE STATION HERE
                    player.closeContainer();

                    try {
                        player.displayClientMessage(Component.literal("test"), true);
                        Thread.sleep(9000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Methods.openPlanetGui(player);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
