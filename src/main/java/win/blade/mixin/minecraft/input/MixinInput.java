package win.blade.mixin.minecraft.input;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Input.class)
public class MixinInput {
    @Shadow
    public PlayerInput playerInput;
    @Shadow
    public float movementForward;
    @Shadow
    public float movementSideways;
}