package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.DoAttackEvent;
import cc.silk.event.impl.player.TickEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.keybinding.KeyUtils;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class WTap extends Module {
    public static final NumberSetting chance = new NumberSetting("Chance (%)", 1, 100, 100, 1);
    private final NumberSetting msDelay = new NumberSetting("Delay (ms)", 1, 500, 80, 1);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true);

    private boolean isWTapping = false;
    private final TimerUtil timer = new TimerUtil();

    public WTap() {
        super("WTap", "Resets sprint by physically releasing W", -1, Category.COMBAT);
        this.addSettings(msDelay, chance, onlyOnGround);
    }

    @EventHandler
    private void onAttackEvent(DoAttackEvent event) {
        if (isNull()) return;
        if (Math.random() * 100 > chance.getValueFloat()) return;

        var target = mc.targetedEntity;
        if (target == null || !target.isAlive()) return;

        if (onlyOnGround.getValue() && !mc.player.isOnGround()) return;

        // Only activate if we are actually holding W and sprinting
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W) || !mc.player.isSprinting()) return;

        if (!isWTapping) {
            isWTapping = true;
            mc.options.forwardKey.setPressed(false); // Physically release W
            timer.reset();
        }
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (isWTapping) {
            // Wait for the delay to finish
            if (timer.hasElapsedTime(msDelay.getValueInt(), true)) {
                // IMPORTANT: Only press W again if the user is still holding the physical key.
                // This prevents the module from walking you forward if you stopped playing.
                if (KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) {
                    mc.options.forwardKey.setPressed(true);
                }
                isWTapping = false;
            }
        }
    }

    @Override
    public void onDisable() {
        // Safety: Ensure W is pressed back down if we disable mid-combo
        if (isWTapping && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) {
            mc.options.forwardKey.setPressed(true);
        }
        isWTapping = false;
        super.onDisable();
    }
}
