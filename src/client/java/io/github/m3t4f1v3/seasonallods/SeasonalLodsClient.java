package io.github.m3t4f1v3.seasonallods;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.github.m3t4f1v3.seasonallods.dto.BiomeReplacement;
import io.netty.buffer.ByteBuf;
import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
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

public class SeasonalLodsClient implements ClientModInitializer {

	private static final VanillaNetworkRegistry registry = VanillaNetworkRegistry.create("seasonallods");
    private static final PacketDefinition<SeasonPacket, FriendlyByteBuf> SYNC_SEASON_PACKET = registry.registerClientbound("sync_season", SeasonPacket.CODEC);
    private static final PacketDefinition<SubSeasonPacket, FriendlyByteBuf> SYNC_SUBSEASON_PACKET = registry.registerClientbound("sync_subseason", SubSeasonPacket.CODEC);
    private static final PacketDefinition<BiomesPacket, FriendlyByteBuf> SYNC_BIOMES_PACKET = registry.registerClientbound("sync_biomes", BiomesPacket.CODEC);

	public record SeasonPacket(String season) implements NetworkPacket {
        public static final NetworkCodec<SeasonPacket, FriendlyByteBuf> CODEC = CompositeCodecs.composite(
            NetworkCodecs.STRING_UTF8, SeasonPacket::season,
            SeasonPacket::new
        );
        
        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return SYNC_SEASON_PACKET; 
        }
    }

    public record SubSeasonPacket(int subSeason) implements NetworkPacket {
        public static final NetworkCodec<SubSeasonPacket, FriendlyByteBuf> CODEC = CompositeCodecs.composite(
            NetworkCodecs.INT, SubSeasonPacket::subSeason,
            SubSeasonPacket::new
        );
        
        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return SYNC_SUBSEASON_PACKET; 
        }
    }

    public record BiomesPacket(String json) implements NetworkPacket {
        public static final NetworkCodec<BiomesPacket, FriendlyByteBuf> CODEC = CompositeCodecs.composite(
            NetworkCodecs.STRING_UTF8, BiomesPacket::json,
            BiomesPacket::new
        );
        
        @Override
        public PacketDefinition<? extends NetworkPacket, ? extends ByteBuf> getDefinition() {
            return SYNC_BIOMES_PACKET; 
        }
    }

	private static int reloadInstance() {
		if (SeasonalLodsConfig.INSTANCE.reloadOnSeasonChange) {
			var instance = (VoxyClientInstance)VoxyCommon.getInstance();
			if (instance == null) {
				return 1;
			}
			var wr = Minecraft.getInstance().levelRenderer;
			if (wr!=null) {
				((IGetVoxyRenderSystem)wr).shutdownRenderer();
			}

			VoxyCommon.shutdownInstance();
			System.gc();
			VoxyCommon.createInstance();

			var r = Minecraft.getInstance().levelRenderer;
			if (r != null) r.allChanged();
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sender.sendPacket(new ServerboundCustomPayloadPacket(new RegistrationPayload(RegistrationPayload.REGISTER, List.of(ResourceLocation.fromNamespaceAndPath("seasonallods", "sync_season"), ResourceLocation.fromNamespaceAndPath("seasonallods", "sync_subseason"), ResourceLocation.fromNamespaceAndPath("seasonallods", "sync_biomes")))));
        });

		registry.addClientboundHandler(SYNC_SEASON_PACKET, (packet, ctx) -> {
            String season = packet.season();

            ctx.getClient().execute(() -> {
                // System.out.println("Received season: " + season);
                if (!season.equalsIgnoreCase(SeasonalReplacement.currentSeason)) {
					SeasonalReplacement.currentSeason = season;
					reloadInstance();
				} else {
					SeasonalReplacement.currentSeason = season;
				}
            });
        });

        registry.addClientboundHandler(SYNC_SUBSEASON_PACKET, (packet, ctx) -> {
            int subSeason = packet.subSeason();

            ctx.getClient().execute(() -> {
                // System.out.println("Received sub-season phase: " + subSeason);
				if (subSeason != SeasonalReplacement.currentSubSeasonPhase) {
					SeasonalReplacement.currentSubSeasonPhase = subSeason;
					reloadInstance();
				} else {
					SeasonalReplacement.currentSubSeasonPhase = subSeason;
				}
            });
        });

        registry.addClientboundHandler(SYNC_BIOMES_PACKET, (packet, ctx) -> {
            String json = packet.json();

            ctx.getClient().execute(() -> {
                System.out.println("Received biome JSON!");

                var type = new TypeToken<Map<String, BiomeReplacement>>(){}.getType();
                Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
                Map<String, BiomeReplacement> parsed = gson.fromJson(json, type);
                SeasonalReplacement.biomeReplacements.putAll(parsed);
            });
        });
	}
}