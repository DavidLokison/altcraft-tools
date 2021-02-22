package mod.altcraft.tools.mixin;

import java.util.Iterator;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import mod.altcraft.tools.recipe.ToolPart;
import mod.altcraft.tools.recipe.ToolPartRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ShapedRecipe.Serializer.class)
public class ShapedRecipeSerializerMixin {

	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void altcraft$onReadJson(Identifier identifier, JsonObject json, CallbackInfoReturnable<ShapedRecipe> ci, String group, Map<String, Ingredient> components, String[] pattern, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result) {
		if (ToolPartRecipe.class.isAssignableFrom(ShapedRecipe.class)) {
			ShapedRecipe recipe = new ShapedRecipe(identifier, group, width, height, ingredients, result);
			if (JsonHelper.hasElement(json, ToolPart.JSON_IDENTIFIER)) {
				DefaultedList<ToolPart> toolparts = DefaultedList.ofSize(ingredients.size(), ToolPart.NONE);
				JsonObject toolpartJson = JsonHelper.getObject(json, ToolPart.JSON_IDENTIFIER);
				for (Map.Entry<String, JsonElement> entry : toolpartJson.entrySet()) {
					if (entry.getKey().length() != 1) {
						throw new JsonSyntaxException("Invalid key entry: '" + (String) entry.getKey() + "' is an invalid symbol (must be 1 character only).");
					}
					if (" ".equals(entry.getKey())) {
						throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
					}
					Ingredient ingredient = components.get(entry.getKey());
					if (ingredient == null) {
						throw new JsonSyntaxException("Defined symbol '" + entry.getKey() + "' isn't defined in the key");
					}
					Identifier registry = new Identifier(entry.getValue().getAsString());
					for (int i = 0; i < ingredients.size(); ++i) {
						if (ingredients.get(i).equals(ingredient)) {
							toolparts.set(i, ToolPart.fromIdentifier(registry, result));
						}
					}
				}
				((ToolPartRecipe) recipe).setToolparts(toolparts);
			}
			ci.setReturnValue(recipe);
		}
	}

	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void altcraft$onReadPacket(Identifier identifier, PacketByteBuf buf, CallbackInfoReturnable<ShapedRecipe> ci, int width, int height, String group, DefaultedList<Ingredient> ingredients, ItemStack result) {
		if (ToolPartRecipe.class.isAssignableFrom(ShapedRecipe.class)) {
			ShapedRecipe recipe = new ShapedRecipe(identifier, group, width, height, ingredients, result);
			boolean hasParts = buf.readBoolean();
			if (hasParts) {
				DefaultedList<ToolPart> toolparts = DefaultedList.ofSize(ingredients.size(), ToolPart.NONE);
				for (int i = 0; i < toolparts.size(); ++i) {
					toolparts.set(i, ToolPart.fromPacket(buf));
				}
				((ToolPartRecipe) recipe).setToolparts(toolparts);
			}
			ci.setReturnValue(recipe);
		}
	}

	@Inject(method = "write", at = @At("TAIL"))
	private void altcraft$onWritePacket(PacketByteBuf buf, ShapedRecipe recipe, CallbackInfo ci) {
		if (recipe instanceof ToolPartRecipe) {
			DefaultedList<ToolPart> toolparts = ((ToolPartRecipe) recipe).getToolparts();
			buf.writeBoolean(toolparts != null);
			if (toolparts != null) {
				Iterator<ToolPart> toolpartIt = toolparts.iterator();
				while (toolpartIt.hasNext()) {
					toolpartIt.next().write(buf);
				}
			}
		}
	}

}
