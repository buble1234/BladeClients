package win.blade.mixin.minecraft.client;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Автор: NoCap
 * Дата создания: 04.08.2025
 */
@Mixin(Scoreboard.class)
public class MixinScoreboard {

    @Inject(method = "removeScoreHolderFromTeam", at = @At("HEAD"), cancellable = true)
    private void onremoveScoreHolderFromTeam(String scoreHolderName, Team team, CallbackInfo ci) {
        Scoreboard scoreboard = (Scoreboard) (Object) this;
        Team actualTeam = scoreboard.getScoreHolderTeam(scoreHolderName);

        if (actualTeam != team) {
            ci.cancel();
        }
    }
}