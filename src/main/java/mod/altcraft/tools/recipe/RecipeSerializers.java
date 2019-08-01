package mod.altcraft.tools.recipe;

import mod.altcraft.tools.Altcraft;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeSerializers {
  
  public static final RecipeSerializer<ShapedHandledRecipe> SHAPED_HANDLED = new ShapedHandledRecipe.Serializer();
  
  public static void registerRecipeSerializers() {
    Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Altcraft.NAMESPACE, "crafting_shaped_handled"), SHAPED_HANDLED);
  }

}
