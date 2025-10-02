package win.blade.mixin.minecraft.input;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import win.blade.common.utils.player.InputAddition;

@Mixin(Input.class)
public abstract class MixinInput implements InputAddition {

    @Unique
    protected PlayerInput initial = PlayerInput.DEFAULT;

    @Unique
    protected PlayerInput untransformed = PlayerInput.DEFAULT;

    @Override
    public PlayerInput getInitial() {
        return initial;
    }

    @Override
    public PlayerInput getUntransformed() {
        return untransformed;
    }

}
