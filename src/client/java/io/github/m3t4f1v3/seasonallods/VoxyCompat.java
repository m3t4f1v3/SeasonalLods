package io.github.m3t4f1v3.seasonallods;

import me.cortex.voxy.commonImpl.VoxyCommon;
import net.minecraft.client.Minecraft;

public class VoxyCompat {
    public static void reloadVoxy() {
        if (VoxyCommon.getInstance() == null) {
            Minecraft.getInstance().levelRenderer.allChanged();
        } else {
            Minecraft.getInstance().player.connection.sendCommand("voxy reload");
        }
    }
}
