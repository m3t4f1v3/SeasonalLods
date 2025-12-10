package io.github.m3t4f1v3.seasonallods;

import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.m3t4f1v3.seasonallods.dto.BiomeReplacement;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.modernnetworking.api.CompositeCodecs;
import xyz.bluspring.modernnetworking.api.NetworkCodec;
import xyz.bluspring.modernnetworking.api.NetworkCodecs;
import xyz.bluspring.modernnetworking.api.NetworkPacket;
import xyz.bluspring.modernnetworking.api.PacketDefinition;
import xyz.bluspring.modernnetworking.api.minecraft.VanillaNetworkRegistry;
import xyz.bluspring.modernnetworking.api.minecraft.VanillaPacketSender;

public class SeasonalLodsClient implements ClientModInitializer {

    private static final VanillaNetworkRegistry registry = VanillaNetworkRegistry.create("seasonallods");
    private static final PacketDefinition<InitialSyncPacket, FriendlyByteBuf> SEASON_PACKET = registry
            .registerClientbound("set_season_data", InitialSyncPacket.CODEC);

    public record InitialSyncPacket(String json, String season, int subSeason, boolean useSubSeasons)
            implements NetworkPacket {
        public static final NetworkCodec<InitialSyncPacket, ByteBuf> CODEC = CompositeCodecs.composite(
                NetworkCodecs.STRING_UTF8, InitialSyncPacket::json,
                NetworkCodecs.STRING_UTF8, InitialSyncPacket::season,
                NetworkCodecs.INT, InitialSyncPacket::subSeason,
                NetworkCodecs.BOOL, InitialSyncPacket::useSubSeasons,
                InitialSyncPacket::new);

        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return SEASON_PACKET;
        }
    }

    public static final PacketDefinition<GameplaySyncPacket, ByteBuf> GAMEPLAY_SYNC_PACKET = registry
            .registerClientbound("sync_season", GameplaySyncPacket.CODEC);

    public record GameplaySyncPacket(String season, int subSeason, boolean useSubSeasons) implements NetworkPacket {
        public static final NetworkCodec<GameplaySyncPacket, ByteBuf> CODEC = CompositeCodecs.composite(
                NetworkCodecs.STRING_UTF8, GameplaySyncPacket::season,
                NetworkCodecs.INT, GameplaySyncPacket::subSeason,
                NetworkCodecs.BOOL, GameplaySyncPacket::useSubSeasons,
                GameplaySyncPacket::new);

        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return GAMEPLAY_SYNC_PACKET;
        }
    }

    private static final PacketDefinition<DiscoverPacket, FriendlyByteBuf> DISCOVER_PACKET = registry
            .registerDual("discover_packet", DiscoverPacket.CODEC);

    public record DiscoverPacket() implements NetworkPacket {
        public static final NetworkCodec<DiscoverPacket, ByteBuf> CODEC = NetworkCodecs.unit(new DiscoverPacket());

        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return DISCOVER_PACKET;
        }
    }

    public static void reloadInstance() {
        if (SeasonalLodsConfig.INSTANCE.reloadOnSeasonChange) {
            if (FabricLoader.getInstance().isModLoaded("voxy")) {
                VoxyCompat.reloadVoxy();
            } else {
                Minecraft.getInstance().levelRenderer.allChanged();
            }
        }
    }

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as
        // rendering.

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sender.sendPacket(new ServerboundCustomPayloadPacket(new RegistrationPayload(RegistrationPayload.REGISTER,
                    List.of(ResourceLocation.fromNamespaceAndPath("seasonallods", "set_season_data"),
                            ResourceLocation.fromNamespaceAndPath("seasonallods", "sync_season"),
                            ResourceLocation.fromNamespaceAndPath("seasonallods", "discover_packet")))));
            VanillaPacketSender.sendToServer(new DiscoverPacket());
        });

        registry.addClientboundHandler(SEASON_PACKET, (packet, ctx) -> {
            String json = packet.json();
            String season = packet.season();
            int subSeason = packet.subSeason();
            boolean useSubSeasons = packet.useSubSeasons();

            ctx.getClient().execute(() -> {
                System.out.println("Received biome JSON!");
                // System.out.println(json);

                var type = new TypeToken<Map<String, BiomeReplacement>>() {
                }.getType();
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                Map<String, BiomeReplacement> parsed = gson.fromJson(json, type);
                SeasonalReplacement.biomeReplacements.putAll(parsed);
                SeasonalReplacement.overrideSeason = true;
                if (!SeasonalReplacement.currentSeason.equals(season) || 
                    SeasonalReplacement.currentSubSeasonPhase != subSeason ||
                    SeasonalReplacement.useSubSeasons != useSubSeasons) {
                    SeasonalReplacement.currentSeason = season;
                    SeasonalReplacement.currentSubSeasonPhase = subSeason;
                    SeasonalReplacement.useSubSeasons = useSubSeasons;
                    reloadInstance();
                }
            });
        });

        registry.addClientboundHandler(GAMEPLAY_SYNC_PACKET, (packet, ctx) -> {
            String season = packet.season();
            int subSeason = packet.subSeason();
            boolean useSubSeasons = packet.useSubSeasons();

            ctx.getClient().execute(() -> {
                if (!SeasonalReplacement.currentSeason.equals(season) || 
                    SeasonalReplacement.currentSubSeasonPhase != subSeason ||
                    SeasonalReplacement.useSubSeasons != useSubSeasons) {
                    SeasonalReplacement.currentSeason = season;
                    SeasonalReplacement.currentSubSeasonPhase = subSeason;
                    SeasonalReplacement.useSubSeasons = useSubSeasons;
                    reloadInstance();
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // SeasonalReplacement.biomeReplacements.clear();
            SeasonalReplacement.overrideSeason = false;
            // SeasonalReplacement.currentSeason = "DISABLED";
            // SeasonalReplacement.currentSubSeasonPhase = 2;
            // SeasonalReplacement.useSubSeasons = false;
        });
    }
}