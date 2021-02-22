package mod.altcraft.tools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

@Mixin(ModelPredicateProviderRegistry.class)
public class ModelPredicateProviderRegistryClientMixin {
	private static final Identifier ALTCRAFT$HANDLE_ID = AltcraftTools.identifier("handle");
	private static final ModelPredicateProvider ALTCRAFT$HANDLE = (stack, world, entity) -> Handle.getRawId(Handle.fromItemStack(stack));

	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private static void altcraft$get(Item item, Identifier id, CallbackInfoReturnable<ModelPredicateProvider> ci) {
		if (item instanceof AltcraftHandledItem) {
			if (ALTCRAFT$HANDLE_ID.equals(id)) {
				ci.setReturnValue(ALTCRAFT$HANDLE);
			}
		}
	}

}
