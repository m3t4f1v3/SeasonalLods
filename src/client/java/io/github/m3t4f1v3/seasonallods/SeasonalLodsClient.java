package io.github.m3t4f1v3.seasonallods;

import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.m3t4f1v3.seasonallods.dto.BiomeReplacement;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
    private static final PacketDefinition<SeasonPacket, FriendlyByteBuf> SEASON_PACKET = registry
            .registerClientbound("sync_season", SeasonPacket.CODEC);
    private static final PacketDefinition<DiscoverPacket, FriendlyByteBuf> DISCOVER_PACKET = registry
            .registerDual("discover_packet", DiscoverPacket.CODEC);

    public record SeasonPacket(String json, String season, int subSeason, boolean useSubSeasons)
            implements NetworkPacket {
        public static final NetworkCodec<SeasonPacket, ByteBuf> CODEC = CompositeCodecs.composite(
                NetworkCodecs.STRING_UTF8, SeasonPacket::json,
                NetworkCodecs.STRING_UTF8, SeasonPacket::season,
                NetworkCodecs.INT, SeasonPacket::subSeason,
                NetworkCodecs.BOOL, SeasonPacket::useSubSeasons,
                SeasonPacket::new);

        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return SEASON_PACKET;
        }
    }

    public record DiscoverPacket() implements NetworkPacket {
        public static final NetworkCodec<DiscoverPacket, ByteBuf> CODEC = NetworkCodecs.unit(new DiscoverPacket());

        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return DISCOVER_PACKET;
        }
    }

    private static void reloadInstance() {
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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("seasonallods")
                    .then(ClientCommandManager.literal("setSeason")
                            .then(ClientCommandManager.argument("season", StringArgumentType.word())
                                    .executes(ctx -> {
                                        String season = StringArgumentType.getString(ctx, "season").toUpperCase();

                                        switch (season) {
                                            case "DISABLED", "SPRING", "SUMMER", "FALL", "WINTER" -> {
                                                SeasonalReplacement.currentSeason = season;
                                                ctx.getSource()
                                                        .sendFeedback(Component.literal("Set season to: " + season));
                                            }
                                            default -> {
                                                ctx.getSource().sendError(Component.literal(
                                                        "Invalid season. Allowed: DISABLED, SPRING, SUMMER, FALL, WINTER"));
                                                return 0;
                                            }
                                        }

                                        reloadInstance();

                                        return 1;
                                    }))));

            dispatcher.register(ClientCommandManager.literal("seasonallods")
                    .then(ClientCommandManager.literal("setSubSeason")
                            .then(ClientCommandManager.argument("phase", IntegerArgumentType.integer(-1, 4))
                                    .executes(ctx -> {
                                        int phase = IntegerArgumentType.getInteger(ctx, "phase");

                                        SeasonalReplacement.currentSubSeasonPhase = phase;
                                        ctx.getSource()
                                                .sendFeedback(Component.literal("Set sub-season phase to: " + phase));
                                        reloadInstance();

                                        return 1;
                                    }))));

            dispatcher.register(ClientCommandManager.literal("seasonallods")
                    .then(ClientCommandManager.literal("getSeason")
                            .executes(ctx -> {
                                ctx.getSource()
                                        .sendFeedback(Component
                                                .literal("Current season: " + SeasonalReplacement.currentSeason));
                                return 1;
                            })));
            dispatcher.register(ClientCommandManager.literal("seasonallods")
                    .then(ClientCommandManager.literal("getSubSeason")
                            .executes(ctx -> {
                                ctx.getSource()
                                        .sendFeedback(Component.literal("Current sub-season phase: "
                                                + SeasonalReplacement.currentSubSeasonPhase));
                                return 1;
                            })));
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sender.sendPacket(new ServerboundCustomPayloadPacket(new RegistrationPayload(RegistrationPayload.REGISTER,
                    List.of(ResourceLocation.fromNamespaceAndPath("seasonallods", "sync_season"),
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
                boolean shouldReload = false;
                if (!SeasonalReplacement.currentSeason.equals(season) || 
                    SeasonalReplacement.currentSubSeasonPhase != subSeason ||
                    SeasonalReplacement.useSubSeasons != useSubSeasons) {
                    shouldReload = true;
                }
                SeasonalReplacement.currentSeason = season;
                SeasonalReplacement.currentSubSeasonPhase = subSeason;
                SeasonalReplacement.useSubSeasons = useSubSeasons;

                if (shouldReload) {
                    reloadInstance();
                }
            });
        });

        // registry.addClientboundHandler(RELOAD_RENDERER_PACKET, (packet, ctx) -> {
        // ctx.getClient().execute(() -> {
        // reloadInstance();
        // });
        // });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // SeasonalReplacement.biomeReplacements.clear();
            SeasonalReplacement.currentSeason = "DISABLED";
            SeasonalReplacement.currentSubSeasonPhase = 2;
        });
    }
}