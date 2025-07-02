package win.blade.core.module.storage.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.settings.impl.BindSetting;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.utils.aim.base.ViewTracer;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.Render3DUtilities;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static win.blade.common.utils.minecraft.ChatUtility.print;

/**
 * Автор Ieo117
 * Дата создания: 26.06.2025, в 19:18:50
 */
@ModuleInfo(name = "AncientXRay", category = Category.MISC)
public class AncientXRay extends Module {
    public BindSetting setting = new BindSetting(this, "Кнопка поиска", -1);
    public ModeSetting setting23 = new ModeSetting(this, "Render type", "2d", "3d");

    public Set<BlockPos> positions = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void key(InputEvents.Keyboard e){
        if(e.getKey() == setting.getValue()) {
            int size = positions.size();

            BlockPos start = mc.player.getBlockPos().add(- 50, - 30, - 50);
            BlockPos end = mc.player.getBlockPos().add(50, 30, 50);

            for (BlockPos pos : BlockPos.stream(start, end).map(BlockPos::toImmutable).toList()) {

                if (mc.world.getBlockState(pos).getBlock() == Blocks.ANCIENT_DEBRIS) {

                    if (isVisible(pos)) {
                        if (mc.world.getBlockState(pos).getBlock() == (Blocks.ANCIENT_DEBRIS)) {
                            positions.add(pos);
                        }
                    }
                }
            }
            int count = (positions.size() - size);
            if (count > 0) {
                print("найдено: " + count );
            }
        }

        if(e.getKey() == GLFW.GLFW_KEY_DELETE){
            print("удалено: " + positions.size());
            positions.clear();
        }
    }

    private boolean isVisible(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos nearPos = pos.offset(direction);
            BlockState nearState = mc.world.getBlockState(nearPos);

            if (nearState.isAir() || nearState.getBlock() == (Blocks.LAVA)) {
                for (Direction direction1 : Direction.values()) {
                    BlockPos secPos = nearPos.offset(direction1);
                    BlockState secState = mc.world.getBlockState(secPos);

                    if (secState.isAir() || secState.getBlock() == (Blocks.LAVA)) {
                        return !couldBeFake(direction1, direction, secPos);
                    }
                }
            }
        }
        return false;
    }

    public boolean couldBeFake(Direction alreadyChecked, Direction mainDirect, BlockPos toCheck) {
        int count = 0;

        for(BlockPos pos : BlockPos.stream(toCheck.add(-1, -1, -1), toCheck.add(1, 1, 1))
                .map(BlockPos::toImmutable).toList()) {

            BlockState state = mc.world.getBlockState(pos);

            if (!(state.isAir() || state.getBlock() == (Blocks.LAVA))) {
                count++;
                if(state.getBlock() == (Blocks.NETHER_GOLD_ORE) || state.getBlock() ==  (Blocks.NETHER_QUARTZ_ORE) || state.getBlock() == (Blocks.ANCIENT_DEBRIS)){
                    count += 2;
                }
            }

            if (count >= 23) return true;
        }

        return false;
    }

    @EventHandler
    public void onWorld(RenderEvents.World e) {
        if(setting23.is("3d")) {
            positions.forEach(position -> {
                if (mc.world.getBlockState(position).getBlock() == Blocks.ANCIENT_DEBRIS) {
                    Render3DUtilities.drawOutline(new Box(position), - 1, 2);
                } else {
                    positions.remove(position);
                }
            });
        }
    }


    @EventHandler
    public void onRender(RenderEvents.Screen e){
        if(setting23.is("2d")) {
            positions.forEach(position -> {
                if (mc.world.getBlockState(position).getBlock() == Blocks.ANCIENT_DEBRIS) {
                    draw2DBlockBox(position, 40, true, ColorUtility.applyAlpha(- 1, 0), Color.WHITE);
                } else {
                    positions.remove(position);
                }
            });
        }
    }


    public void draw2DBlockBox(BlockPos pos, float pSize, boolean showDistance, int textColor, Color outlineColor){
        Vec3d vec3 = pos.toCenterPos();

        double distance = ViewTracer.distanceTo(mc.player, vec3);
//        float scale = (float) MathUtility.getScale(vec3, 0.125 + distance / 128);
        int size = (int) (12 - (distance / 10));
        Vec3d vec2 = MathUtility.worldSpaceToScreenSpace(vec3);
//        pSize *= scale;

        double x = vec2.getX();
        double y = vec2.getY();

        Builder.border()
                .color(new QuadColorState(outlineColor))
                .size(new SizeState(pSize, pSize))
                .radius(new QuadRadiusState(2))
                .thickness(0.5f)
                .build()
                .render(x - pSize / 2, y - pSize / 2);
        if (showDistance) {
            String text = String.format("%.1f", distance);
            BuiltText builtText = Builder.text()
                    .text(text)
                    .font(FontType.sf_regular.get())
                    .size(size / 2)
                    .color(textColor)
                    .build();

            builtText.render(x - builtText.font().getWidth(text, size / 2) / 2, y - 2);
        }
    }

}
