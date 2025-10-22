package gwisp.flight.lovermods.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
    @Shadow @Final private GameProfile profile;

    private static final Map<String, String> CAPE_MAP = new HashMap<>();

    /*
    Capes are given out to special people.
     */
    static {
        CAPE_MAP.put("Gwisp", "gwisp_cape");
        CAPE_MAP.put("UrFaveFriday", "fridaycape");
        CAPE_MAP.put("LoverMods", "lovermodsminecon");
        CAPE_MAP.put("Collexl", "collexxl");
    }

    @Inject(
            method = "method_52810",
            at = @At("TAIL"),
            order = 9999,
            cancellable = true
    )
    private void overrideCape(CallbackInfoReturnable<SkinTextures> cir) {
        SkinTextures original = cir.getReturnValue();
        String playerName = profile.getName();

        if (CAPE_MAP.containsKey(playerName)) {
            String capeName = CAPE_MAP.get(playerName);
            Identifier customCape = Identifier.of("lovermods", "textures/entity/" + capeName + ".png");
            SkinTextures modified = new SkinTextures(
                    original.texture(),
                    original.textureUrl(),
                    customCape,
                    original.elytraTexture(),
                    original.model(),
                    original.secure()
            );
            cir.setReturnValue(modified);
        }
    }
}