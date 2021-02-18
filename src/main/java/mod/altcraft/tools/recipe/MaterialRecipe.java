package mod.altcraft.tools.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

public interface MaterialRecipe extends Recipe<Inventory> {
	default RecipeType<?> getType() {
		return RecipeTypes.MATERIAL;
	}
}
