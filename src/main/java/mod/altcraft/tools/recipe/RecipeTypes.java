package mod.altcraft.tools.recipe;

import mod.altcraft.tools.AltcraftTools;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeTypes {
	public static final RecipeType<MaterialRecipe> MATERIAL;

	private static <T extends Recipe<?>> RecipeType<T> register(String string, RecipeType<T> recipeType) {
		return register(AltcraftTools.identifier(string), recipeType);
	}

	private static <T extends Recipe<?>> RecipeType<T> register(Identifier identifier, RecipeType<T> recipeType) {
		return Registry.register(Registry.RECIPE_TYPE, identifier, recipeType);
	}

	static {
		MATERIAL = new RecipeType<MaterialRecipe>() {
			public String toString() {
				return Registry.RECIPE_TYPE.getId(this).toString();
			}
		};
	}

	public static void registerRecipeTypes() {
		register("material", MATERIAL);
	}

}
