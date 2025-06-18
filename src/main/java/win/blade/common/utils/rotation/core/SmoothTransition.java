package win.blade.common.utils.rotation.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface SmoothTransition {
    ViewDirection interpolate(ViewDirection current, ViewDirection target);

    default ViewDirection interpolate(ViewDirection current, ViewDirection target, Vec3d targetPos, Entity targetEntity) {
        return interpolate(current, target);
    }
}