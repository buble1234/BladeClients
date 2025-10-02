package win.blade.common.utils.player;

import net.minecraft.util.PlayerInput;

public interface InputAddition {
    PlayerInput getInitial();
    PlayerInput getUntransformed();
}
