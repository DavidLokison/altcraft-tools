package mod.altcraft.tools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.altcraft.tools.recipe.RecipeTypes;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.Recipe;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookClientMixin {

	@Inject(method = "getGroupForRecipe", at = @At("HEAD"), cancellable = true)
	private static void altcraft$truncateWarnings(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookGroup> ci) {
		if (recipe.getType().equals(RecipeTypes.MATERIAL)) {
			ci.setReturnValue(RecipeBookGroup.UNKNOWN);
		}
	}

}
