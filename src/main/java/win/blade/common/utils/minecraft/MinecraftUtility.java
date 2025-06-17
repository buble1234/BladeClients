package win.blade.common.utils.minecraft;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.util.Uuids;
import win.blade.mixin.accessor.MinecraftClientAccessor;

import java.util.Optional;

public class MinecraftUtility {

    public static Session newSession(String name){
        return new Session(name, Uuids.getOfflinePlayerUuid(name), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
    }

    public static void setSession(Session session) throws AuthenticationException {
        MinecraftClient mc = MinecraftClient.getInstance();
        MinecraftClientAccessor minecraftClientAccessor = (MinecraftClientAccessor) mc;
        minecraftClientAccessor.setSession(session);
        UserApiService apiService;
        apiService = minecraftClientAccessor.getAuthenticationService().createUserApiService(session.getAccessToken());
        minecraftClientAccessor.setUserApiService(apiService);
        minecraftClientAccessor.setSocialInteractionsManager(new SocialInteractionsManager(mc, apiService));
        minecraftClientAccessor.setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        minecraftClientAccessor.setAbuseReportContext(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }
}
