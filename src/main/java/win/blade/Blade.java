package win.blade;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;
import win.blade.common.utils.browser.BrowserManager;
import win.blade.common.utils.minecraft.MinecraftUtility;
import win.blade.core.Manager;

import java.util.Optional;

public class Blade implements ModInitializer {

    public static Manager manager = new Manager();

    @Override
    public void onInitialize() {
        manager.init();
        try {
            Session newSession = new Session("MagnatYgla", Uuids.getOfflinePlayerUuid("MagnatYgla"), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
            MinecraftUtility.setSession(newSession);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }
}