package mod.altcraft.tools.recipe;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ShapedHandledRecipe extends ShapedRecipe implements CraftingRecipe {
	private final DefaultedList<Ingredient> inputs;
	private final DefaultedList<ToolPart> toolparts;
	private final ItemStack output;
	private final String group;
	private final int width;
	private final int height;

	public ShapedHandledRecipe(Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> ingredients, DefaultedList<ToolPart> toolparts, ItemStack result) {
		super(identifier, group, width, height, ingredients, result);
		this.inputs = ingredients;
		this.toolparts = toolparts;
		this.output = result;
		this.group = group;
		this.width = width;
		this.height = height;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.SHAPED_HANDLED;
	}

	@Override
	public DefaultedList<Ingredient> getPreviewInputs() {
		DefaultedList<Ingredient> inputs = DefaultedList.ofSize(this.inputs.size(), Ingredient.EMPTY);
		for (int i = 0; i < this.inputs.size(); ++i) {
			if (toolparts.get(i) != ToolPart.NONE) {
				inputs.set(i, toolparts.get(i).getIngredient());
			} else {
				inputs.set(i, this.inputs.get(i));
			}
		}
		return inputs;
	}

	@Override
	public boolean matches(CraftingInventory inventory, World world) {
		for (int x = 0; x <= inventory.getWidth() - this.width; ++x) {
			for (int y = 0; y <= inventory.getHeight() - this.height; ++y) {
				if (this.matchesSmall(inventory, x, y, true, null)) {
					return true;
				}
				if (this.matchesSmall(inventory, x, y, false, null)) {
					return true;
				}
			}
		}
		return false;
	}

	private Map<ToolPart, ItemStack> getToolpartMaterials(CraftingInventory inventory) {
		Map<ToolPart, ItemStack> toolparts;
		for (int x = 0; x <= inventory.getWidth() - this.width; ++x) {
			for (int y = 0; y <= inventory.getHeight() - this.height; ++y) {
				toolparts = Maps.newHashMap();
				if (this.matchesSmall(inventory, x, y, true, toolparts)) {
					return toolparts;
				}
				toolparts = Maps.newHashMap();
				if (this.matchesSmall(inventory, x, y, false, toolparts)) {
					return toolparts;
				}
			}
		}
		return null;
	}

	private boolean matchesSmall(CraftingInventory inventory, int offsX, int offsY, boolean mirror, Map<ToolPart, ItemStack> toolparts) {
		if (toolparts == null) {
			toolparts = Maps.newHashMap();
		}
		DefaultedList<Ingredient> ingredients = this.inputs;
		for (int baseX = 0; baseX < inventory.getWidth(); ++baseX) {
			for (int baseY = 0; baseY < inventory.getHeight(); ++baseY) {
				int x = baseX - offsX;
				int y = baseY - offsY;
				Ingredient ingredient = Ingredient.EMPTY;
				ToolPart toolpart = ToolPart.NONE;
				if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
					int index = 0;
					if (mirror) {
						index = this.width - x - 1 + y * this.width;
					} else {
						index = x + y * this.width;
					}
					ingredient = ingredients.get(index);
					toolpart = this.toolparts.get(index);
				}
				ItemStack stack = inventory.getInvStack(baseX + baseY * inventory.getWidth());
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
	public ItemStack getOutput() {
		return this.output;
	}

	@Override
	public ItemStack craft(CraftingInventory inventory) {
		ItemStack output = this.getOutput().copy();
		Map<ToolPart, ItemStack> toolparts = this.getToolpartMaterials(inventory);
		for (ToolPart part : toolparts.keySet()) {
			part.addTag(output, toolparts.get(part));
		}
		return output;
	}

	public static class Serializer implements RecipeSerializer<ShapedHandledRecipe> {

		@Override
		public void write(PacketByteBuf buf, ShapedHandledRecipe recipe) {
			buf.writeVarInt(recipe.width);
			buf.writeVarInt(recipe.height);
			buf.writeString(recipe.group);
			Iterator<Ingredient> ingredientIt = recipe.inputs.iterator();
			while (ingredientIt.hasNext()) {
				ingredientIt.next().write(buf);
			}
			Iterator<ToolPart> toolpartIt = recipe.toolparts.iterator();
			while (toolpartIt.hasNext()) {
				toolpartIt.next().write(buf);
			}
			buf.writeItemStack(recipe.output);
		}

		@Override
		public ShapedHandledRecipe read(Identifier identifier, PacketByteBuf buf) {
			int width = buf.readVarInt();
			int height = buf.readVarInt();
			String group = buf.readString();
			DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
			DefaultedList<ToolPart> toolparts = DefaultedList.ofSize(width * height, ToolPart.NONE);
			for (int i = 0; i < ingredients.size(); ++i) {
				ingredients.set(i, Ingredient.fromPacket(buf));
			}
			for (int i = 0; i < toolparts.size(); ++i) {
				toolparts.set(i, ToolPart.fromPacket(buf));
			}
			ItemStack result = buf.readItemStack();
			return new ShapedHandledRecipe(identifier, group, width, height, ingredients, toolparts, result);
		}

		@Override
		public ShapedHandledRecipe read(Identifier identifier, JsonObject json) {
			String group = JsonHelper.getString(json, "group", "");
			ItemStack stack = Serializer.getItemStack(JsonHelper.getObject(json, "result"));
			Map<String, Ingredient> components = Maps.newHashMap();
			Map<String, ToolPart> parts = Maps.newHashMap();
			Serializer.getComponents(JsonHelper.getObject(json, "key"), components, parts, stack);
			String[] pattern = Serializer.combinePattern(Serializer.getPattern(JsonHelper.getArray(json, "pattern")));
			int width = pattern[0].length();
			int height = pattern.length;
			DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
			DefaultedList<ToolPart> toolparts = DefaultedList.ofSize(width * height, ToolPart.NONE);
			Serializer.getIngredients(pattern, components, parts, width, height, ingredients, toolparts);
			return new ShapedHandledRecipe(identifier, group, width, height, ingredients, toolparts, stack);
		}

		public static ItemStack getItemStack(JsonObject json) {
			String itemId = JsonHelper.getString(json, "item");
			Item item = (Item) Registry.ITEM.getOrEmpty(new Identifier(itemId)).orElseThrow(() -> {
				return new JsonSyntaxException("Unknown item '" + itemId + "'");
			});
			if (!(item instanceof AltcraftHandledItem)) {
				throw new IllegalArgumentException("'" + itemId + "' is not handled");
			}
			if (json.has("data")) {
				throw new JsonParseException("Disallowed data tag found");
			} else {
				int count = JsonHelper.getInt(json, "count", 1);
				ItemStack stack = new ItemStack(item, count);
				return stack;
			}
		}

		private static void getComponents(JsonObject json, Map<String, Ingredient> components, Map<String, ToolPart> parts, ItemStack stack) {
			Iterator<Entry<String, JsonElement>> jsonIt = json.entrySet().iterator();
			while (jsonIt.hasNext()) {
				Entry<String, JsonElement> entry = jsonIt.next();
				if (entry.getKey().length() != 1) {
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
				}
				if (" ".equals(entry.getKey())) {
					throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
				}
				JsonElement value = entry.getValue();
				if (value.isJsonObject()) {
					if (value.getAsJsonObject().has(ToolPart.JSON_IDENTIFIER)) {
						String tag = JsonHelper.getString(value.getAsJsonObject(), ToolPart.JSON_IDENTIFIER);
						parts.put(entry.getKey(), ToolPart.fromIdentifier(new Identifier(tag), stack));
						value.getAsJsonObject().remove(ToolPart.JSON_IDENTIFIER);
						value.getAsJsonObject().addProperty("item", "minecraft:air");
					}
				}
				components.put(entry.getKey(), Ingredient.fromJson(value));
			}
			components.put(" ", Ingredient.EMPTY);
		}

		private static void getIngredients(String[] pattern, Map<String, Ingredient> components, Map<String, ToolPart> parts, int width, int height, List<Ingredient> ingredients, List<ToolPart> toolparts) {
			Set<String> keys = Sets.newHashSet(components.keySet());
			keys.remove(" ");
			for (int y = 0; y < pattern.length; ++y) {
				for (int x = 0; x < pattern[y].length(); ++x) {
					String key = pattern[y].substring(x, x + 1);
					Ingredient ingredient = components.get(key);
					ToolPart toolpart = parts.get(key);
					if (ingredient == null) {
						throw new JsonSyntaxException("Pattern references symbol '" + key + "' but it's not defined in the key");
					}
					keys.remove(key);
					if (parts.containsKey(key)) {
						toolparts.set(x + width * y, toolpart);
					} else {
						ingredients.set(x + width * y, ingredient);
					}
				}
			}
			if (!keys.isEmpty()) {
				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
			}
		}

		static String[] combinePattern(String... pattern) {
			int startX = Integer.MAX_VALUE;
			int endX = 0;
			int startY = 0;
			int ignoreLastY = 0;
			for (int yRead = 0; yRead < pattern.length; ++yRead) {
				String line = pattern[yRead];
				startX = Math.min(startX, findNextIngredient(line));
				int ingredientPosReverse = findNextIngredientReverse(line);
				endX = Math.max(endX, ingredientPosReverse);
				if (ingredientPosReverse < 0) {
					if (startY == yRead) {
						++startY;
					}
					++ignoreLastY;
				} else {
					ignoreLastY = 0;
				}
			}
			if (pattern.length == ignoreLastY) {
				return new String[0];
			} else {
				String[] combined = new String[pattern.length - ignoreLastY - startY];

				for (int yWrite = 0; yWrite < combined.length; ++yWrite) {
					combined[yWrite] = pattern[yWrite + startY].substring(startX, endX + 1);
				}
				return combined;
			}
		}

		private static int findNextIngredient(String line) {
			int pos = 0;
			while (pos < line.length() && line.charAt(pos) == ' ') {
				++pos;
			}
			return pos;
		}

		private static int findNextIngredientReverse(String line) {
			int pos = line.length() - 1;
			while (pos >= 0 && line.charAt(pos) == ' ') {
				--pos;
			}
			return pos;
		}

		private static String[] getPattern(JsonArray array) {
			String[] pattern = new String[array.size()];
			if (pattern.length > 3) {
				throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
			} else if (pattern.length == 0) {
				throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
			} else {
				for (int i = 0; i < pattern.length; ++i) {
					String line = JsonHelper.asString(array.get(i), "pattern[" + i + "]");
					if (line.length() > 3) {
						throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
					}
					if (i > 0 && pattern[0].length() != line.length()) {
						throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
					}
					pattern[i] = line;
				}
				return pattern;
			}
		}
	}

}
