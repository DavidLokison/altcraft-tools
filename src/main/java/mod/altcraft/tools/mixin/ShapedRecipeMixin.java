package mod.altcraft.tools.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.recipe.ToolPart;
import mod.altcraft.tools.recipe.ToolPartRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin implements ToolPartRecipe {
	private DefaultedList<ToolPart> altcraft$toolparts;
	@Shadow
	private DefaultedList<Ingredient> inputs;
	@Shadow
	private int width;
	@Shadow
	private int height;

	@Shadow
	private ItemStack getOutput() {
		throw new UnsupportedOperationException("Mixin Dummy");
	}

	@Shadow
	private boolean matchesSmall(CraftingInventory inv, int offsetX, int offsetY, boolean mirror) {
		throw new UnsupportedOperationException("Mixin Dummy");
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void altcraft$onInit(Identifier id, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack output, CallbackInfo ci) {
		this.altcraft$toolparts = null;
	}

	@Inject(method = "getPreviewInputs", at = @At("HEAD"), cancellable = true)
	private void altcraft$getPreviewInputs(CallbackInfoReturnable<DefaultedList<Ingredient>> ci) {
		if (altcraft$toolparts != null) {
			DefaultedList<Ingredient> inputs = DefaultedList.ofSize(this.inputs.size(), Ingredient.EMPTY);
			for (int i = 0; i < this.inputs.size(); ++i) {
				if (altcraft$toolparts.get(i) != ToolPart.NONE) {
					inputs.set(i, altcraft$toolparts.get(i).getIngredient());
				} else {
					inputs.set(i, this.inputs.get(i));
				}
			}
			ci.setReturnValue(inputs);
		}
	}

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	private void altcraft$craft(CraftingInventory inventory, CallbackInfoReturnable<ItemStack> ci) {
		if (altcraft$toolparts != null) {
			ItemStack output = this.getOutput().copy();
			Map<ToolPart, ItemStack> toolparts = this.getToolpartMaterials(inventory);
			for (ToolPart part : toolparts.keySet()) {
				part.addTag(output, toolparts.get(part));
			}
			ci.setReturnValue(output);
		}
	}

	@Redirect(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/ShapedRecipe;matchesSmall(Lnet/minecraft/inventory/CraftingInventory;IIZ)Z"), require = 2)
	private boolean altcraft$redirectMatchesSmall(ShapedRecipe recipe /* recipe is this */, CraftingInventory inv, int offsetX, int offsetY, boolean mirror) {
		if (this.altcraft$toolparts != null) {
			return this.altcraft$matchesSmall(inv, offsetX, offsetY, mirror, null);
		} else {
			return this.matchesSmall(inv, offsetX, offsetY, mirror);
		}
	}

	@Override
	public Map<ToolPart, ItemStack> getToolpartMaterials(CraftingInventory inventory) {
		Map<ToolPart, ItemStack> toolparts;
		for (int x = 0; x <= inventory.getWidth() - this.width; ++x) {
			for (int y = 0; y <= inventory.getHeight() - this.height; ++y) {
				toolparts = Maps.newHashMap();
				if (this.altcraft$matchesSmall(inventory, x, y, true, toolparts)) {
					toolparts.forEach((k, v) -> AltcraftTools.LOGGER.info(k.toString()));
					return toolparts;
				}
				toolparts = Maps.newHashMap();
				if (this.altcraft$matchesSmall(inventory, x, y, false, toolparts)) {
					toolparts.forEach((k, v) -> AltcraftTools.LOGGER.info(k.toString()));
					return toolparts;
				}
			}
		}
		return null;
	}

	private boolean altcraft$matchesSmall(CraftingInventory inventory, int offsX, int offsY, boolean mirror, Map<ToolPart, ItemStack> toolparts) {
		if (toolparts == null) {
			toolparts = Maps.newHashMap();
		}
		for (int invX = 0; invX < inventory.getWidth(); ++invX) {
			for (int invY = 0; invY < inventory.getHeight(); ++invY) {
				int recipeX = invX - offsX;
				int recipeY = invY - offsY;
				Ingredient ingredient = Ingredient.EMPTY;
				ToolPart toolpart = ToolPart.NONE;
				if (recipeX >= 0 && recipeY >= 0 && recipeX < this.width && recipeY < this.height) {
					int index = 0;
					if (mirror) {
						index = this.width - recipeX - 1 + recipeY * this.width;
					} else {
						index = recipeX + recipeY * this.width;
					}
					ingredient = this.inputs.get(index);
					toolpart = this.altcraft$toolparts.get(index);
				}
				ItemStack stack = inventory.getStack(invX + invY * inventory.getWidth());
				if (toolpart != ToolPart.NONE) {
					if (!toolpart.test(stack)) {
						return false;
					} else {
						if (toolparts.containsKey(toolpart)) {
							if (!toolparts.get(toolpart).isItemEqualIgnoreDamage(stack)) {
								return false;
							}
						} else {
							toolparts.put(toolpart, stack);
						}
					}
				} else if (!ingredient.test(stack)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void setToolparts(DefaultedList<ToolPart> toolparts) {
		this.altcraft$toolparts = toolparts;
	}

	@Override
	public DefaultedList<ToolPart> getToolparts() {
		return this.altcraft$toolparts;
	}

}
