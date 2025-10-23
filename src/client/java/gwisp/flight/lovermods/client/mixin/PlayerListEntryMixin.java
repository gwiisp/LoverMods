package gwisp.flight.lovermods.client.mixin;

import com.mojang.authlib.GameProfile;
import gwisp.flight.lovermods.client.cosmetics.CosmeticManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
    @Shadow @Final private GameProfile profile;

    @Inject(
            method = "method_52810",
            at = @At("RETURN"),
            cancellable = true
    )
    private void overrideCape(CallbackInfoReturnable<SkinTextures> cir) {
        if (!CosmeticManager.isLoaded()) return;

        SkinTextures original = cir.getReturnValue();
        String playerName = profile.getName();

        String capeName = CosmeticManager.getCapeForPlayer(playerName);
        if (capeName != null) {
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