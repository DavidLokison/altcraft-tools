package mod.altcraft.tools.recipe;

import mod.altcraft.tools.AltcraftTools;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeSerializers {

	public static final RecipeSerializer<MaterialRecipe> MATERIAL;

	static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
		return register(AltcraftTools.identifier(name), serializer);
	}

	static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(Identifier identifier, S serializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, identifier, serializer);
	}

	static {
		MATERIAL = new MaterialRecipe.Serializer();
	}

	public static void registerRecipeSerializers() {
		register("material", MATERIAL);
	}

}
