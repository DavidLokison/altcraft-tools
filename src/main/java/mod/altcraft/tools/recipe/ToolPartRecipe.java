package mod.altcraft.tools.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.util.Registries;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ToolPartRecipe implements MaterialRecipe {
	private final Identifier identifier;
	private final Ingredient ingredient;
	private final ItemStack output;
	private final String handle;

	public ToolPartRecipe(Identifier identifier, Ingredient ingredient, ItemStack result) {
		this.identifier = identifier;
		this.ingredient = ingredient;
		this.output = result;
		this.handle = result.getSubTag(AltcraftTools.NAMESPACE).getString("handle");
		Registries.HANDLE.get(new Identifier(this.handle)).setIngredient(ingredient);
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		return ingredient.test(inv.getStack(0));
	}

	@Override
	public ItemStack craft(Inventory inv) {
		ItemStack stack = inv.getStack(0);
		stack.getOrCreateSubTag(AltcraftTools.NAMESPACE).put("handle", StringTag.of(handle));
		return stack;
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput() {
		return this.output;
	}

	@Override
	public Identifier getId() {
		return this.identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.TOOLPART;
	}

	public static class Serializer implements RecipeSerializer<ToolPartRecipe> {

		public ToolPartRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
			ItemStack result = packetByteBuf.readItemStack();
			return new ToolPartRecipe(identifier, ingredient, result);
		}

		public void write(PacketByteBuf packetByteBuf, ToolPartRecipe recipe) {
			recipe.ingredient.write(packetByteBuf);
			packetByteBuf.writeItemStack(recipe.output);
		}

		public ToolPartRecipe read(Identifier identifier, JsonObject jsonObject) {
			Ingredient ingredient = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient"));
			ItemStack result = getItemStack(JsonHelper.getObject(jsonObject, "result"));
			return new ToolPartRecipe(identifier, ingredient, result);
		}

		public static ItemStack getItemStack(JsonObject json) {
			if (json.entrySet().size() != 1) {
				throw new JsonSyntaxException("Specify exactly one toolpart type!");
			}
			// TODO: Generic Tool Part
			Handle handle = json.entrySet().stream().map(entry -> {
				String registry = entry.getKey();
				String toolpart = entry.getValue().getAsString();
				Handle hdl = (Handle) Registry.REGISTRIES.get(new Identifier(registry)).getOrEmpty(new Identifier(toolpart)).orElse(null);
				if (hdl == null) {
					throw new JsonSyntaxException("Unknown handle '" + toolpart + "'");
				}
				return hdl;
			}).findFirst().get();
			ItemStack stack = new ItemStack(Items.WOODEN_AXE);
			handle.addData(stack);
			return stack;
		}
	}
}
