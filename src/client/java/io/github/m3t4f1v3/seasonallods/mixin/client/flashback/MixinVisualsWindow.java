package io.github.m3t4f1v3.seasonallods.mixin.client.flashback;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.moulberry.flashback.editor.ui.windows.VisualsWindow;

import imgui.flashback.ImGui;
import imgui.flashback.type.ImInt;
import io.github.m3t4f1v3.seasonallods.SeasonalLodsClient;
import io.github.m3t4f1v3.seasonallods.SeasonalReplacement;

@Mixin(VisualsWindow.class)
class MixinVisualsWindow {
    private static final String[] SEASONS = {
            "WINTER",
            "SPRING",
            "SUMMER",
            "FALL"
    };

    private static final String[] SUBSEASONS = {
            "START",
            "EARLY",
            "FULL",
            "MIDDLE",
            "LATE"
    };

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Limgui/flashback/ImGui;end()V", shift = At.Shift.BEFORE))
    private static void seasonallods$addSeasonUi(CallbackInfo ci) {
        ImGui.separator();
        ImGui.text("Seasonal LODs");

        if (ImGui.checkbox("Override Season", SeasonalReplacement.overrideSeason)) {
            SeasonalReplacement.overrideSeason = !SeasonalReplacement.overrideSeason;
            SeasonalLodsClient.reloadInstance();
        }

        if (SeasonalReplacement.overrideSeason) {
            int currentSeasonIndex = 0;
            for (int i = 0; i < SEASONS.length; i++) {
                if (SEASONS[i].equals(SeasonalReplacement.currentSeason)) {
                    currentSeasonIndex = i;
                    break;
                }
            }

            ImInt seasonInt = new ImInt(currentSeasonIndex);

            if (ImGui.combo("Season", seasonInt, SEASONS)) {
                SeasonalReplacement.currentSeason = SEASONS[seasonInt.get()];
                SeasonalLodsClient.reloadInstance();
            }

            int currentSubSeasonIndex = 2;
            if (SeasonalReplacement.useSubSeasons) {
                for (int i = 0; i < SUBSEASONS.length; i++) {
                    if (i == SeasonalReplacement.currentSubSeasonPhase) {
                        currentSubSeasonIndex = i;
                        break;
                    }
                }
            }

            if (ImGui.checkbox("Use Sub-Seasons", SeasonalReplacement.useSubSeasons)) {
                SeasonalReplacement.useSubSeasons = !SeasonalReplacement.useSubSeasons;
                SeasonalLodsClient.reloadInstance();
            }

            if (SeasonalReplacement.useSubSeasons) {
                ImInt subSeasonInt = new ImInt(currentSubSeasonIndex);
                if (ImGui.combo("Sub-Season Phase", subSeasonInt, SUBSEASONS)) {
                    SeasonalReplacement.currentSubSeasonPhase = subSeasonInt.get();
                    SeasonalLodsClient.reloadInstance();
                }
            }

        }
    }
}