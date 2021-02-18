package mod.altcraft.tools.recipe;

import mod.altcraft.tools.AltcraftTools;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeSerializers {

	public static final RecipeSerializer<ShapedHandledRecipe> SHAPED_HANDLED;
	public static final RecipeSerializer<ToolPartRecipe> TOOLPART;

	static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
		return register(new Identifier(AltcraftTools.NAMESPACE, name), serializer);
	}

	static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(Identifier identifier, S serializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, identifier, serializer);
	}

	static {
		SHAPED_HANDLED = new ShapedHandledRecipe.Serializer();
		TOOLPART = new ToolPartRecipe.Serializer();
	}

	public static void registerRecipeSerializers() {
		register("crafting_shaped_handled", SHAPED_HANDLED);
		register("toolpart", TOOLPART);
	}

}
