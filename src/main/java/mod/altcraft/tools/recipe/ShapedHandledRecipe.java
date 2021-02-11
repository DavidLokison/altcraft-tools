package mod.altcraft.tools.recipe;

import java.util.Iterator;
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

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.util.Registries;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
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
  private final Identifier handle;

  public ShapedHandledRecipe(Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result, Identifier handle) {
    super(identifier, group, width, height, ingredients, result);
    this.handle = handle;
  }

  public RecipeSerializer<?> getSerializer() {
    return RecipeSerializers.SHAPED_HANDLED;
  }

  public boolean matches(CraftingInventory inventory, World world) {
    for (int x = 0; x <= inventory.getWidth() - this.getWidth(); ++x) {
      for (int y = 0; y <= inventory.getHeight() - this.getHeight(); ++y) {
        if (this.matchesSmall(inventory, x, y, true)) {
          return true;
        }
        if (this.matchesSmall(inventory, x, y, false)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean matchesSmall(CraftingInventory inventory, int offsX, int offsY, boolean mirror) {
    for (int baseX = 0; baseX < inventory.getWidth(); ++baseX) {
      for (int baseY = 0; baseY < inventory.getHeight(); ++baseY) {
        int x = baseX - offsX;
        int y = baseY - offsY;
        Ingredient ingredient_1 = Ingredient.EMPTY;
        if (x >= 0 && y >= 0 && x < this.getWidth() && y < this.getHeight()) {
          if (mirror) {
            ingredient_1 = this.getPreviewInputs().get(this.getWidth() - x - 1 + y * this.getWidth());
          } else {
            ingredient_1 = this.getPreviewInputs().get(x + y * this.getWidth());
          }
        }
        if (!ingredient_1.test(inventory.getInvStack(baseX + baseY * inventory.getWidth()))) {
          return false;
        }
      }
    }
    return true;
  }

  public ItemStack craft(CraftingInventory inventory) {
    return this.getOutput().copy();
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
      String handleId = JsonHelper.getString(json, "handle");
      Handle handle = Registries.HANDLE.getOrEmpty(new Identifier(handleId)).orElse(null);
      if (handle == null) {
        throw new JsonSyntaxException("Unknown handle '" + handleId + "'");
      } else if (!((AltcraftHandledItem) item).isValidHandle(handle)) {
         throw new IllegalArgumentException("'" + handleId + "' is no valid handle for '" + itemId + "'");
      }
      CompoundTag altcraft = new CompoundTag();
      altcraft.put("handle", StringTag.of(handleId));
      ItemStack stack = new ItemStack(item, count);
      stack.putSubTag(AltcraftTools.NAMESPACE, altcraft);
      return stack;
    }
  }

  private static DefaultedList<Ingredient> getIngredients(String[] pattern, Map<String, Ingredient> components, int width, int height) {
    DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
    Set<String> keys = Sets.newHashSet(components.keySet());
    keys.remove(" ");
    for (int y = 0; y < pattern.length; ++y) {
      for (int x = 0; x < pattern[y].length(); ++x) {
        String key = pattern[y].substring(x, x + 1);
        Ingredient ingredient = components.get(key);
        if (ingredient == null) {
          throw new JsonSyntaxException("Pattern references symbol '" + key + "' but it's not defined in the key");
        }
        keys.remove(key);
        ingredients.set(x + width * y, ingredient);
      }
    }
    if (!keys.isEmpty()) {
      throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
    } else {
      return ingredients;
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

  private static Map<String, Ingredient> getComponents(JsonObject json) {
    Map<String, Ingredient> components = Maps.newHashMap();
    Iterator<Entry<String, JsonElement>> jsonIt = json.entrySet().iterator();
    while (jsonIt.hasNext()) {
      Entry<String, JsonElement> entry = jsonIt.next();
      if (entry.getKey().length() != 1) {
        throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
      }
      if (" ".equals(entry.getKey())) {
        throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
      }
      components.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
    }
    components.put(" ", Ingredient.EMPTY);
    return components;
  }

  public static class Serializer implements RecipeSerializer<ShapedHandledRecipe> {

    @Override
    public void write(PacketByteBuf buf, ShapedHandledRecipe recipe) {
      buf.writeVarInt(recipe.getWidth());
      buf.writeVarInt(recipe.getHeight());
      buf.writeString(recipe.getGroup());
      Iterator<Ingredient> ingredientIt = recipe.getPreviewInputs().iterator();
      while (ingredientIt.hasNext()) {
        ingredientIt.next().write(buf);
      }
      buf.writeItemStack(recipe.getOutput());
      buf.writeIdentifier(recipe.handle);
    }

    @Override
    public ShapedHandledRecipe read(Identifier identifier, PacketByteBuf buf) {
      int width = buf.readVarInt();
      int height = buf.readVarInt();
      String group = buf.readString();
      DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
      for (int i = 0; i < ingredients.size(); ++i) {
        ingredients.set(i, Ingredient.fromPacket(buf));
      }
      ItemStack result = buf.readItemStack();
      Identifier handle = buf.readIdentifier();
      return new ShapedHandledRecipe(identifier, group, width, height, ingredients, result, handle);
    }

    @Override
    public ShapedHandledRecipe read(Identifier identifier, JsonObject json) {
      String group = JsonHelper.getString(json, "group", "");
      Map<String, Ingredient> components = ShapedHandledRecipe.getComponents(JsonHelper.getObject(json, "key"));
      String[] pattern = ShapedHandledRecipe.combinePattern(ShapedHandledRecipe.getPattern(JsonHelper.getArray(json, "pattern")));
      int width = pattern[0].length();
      int height = pattern.length;
      DefaultedList<Ingredient> ingredients = ShapedHandledRecipe.getIngredients(pattern, components, width, height);
      ItemStack stack = ShapedHandledRecipe.getItemStack(JsonHelper.getObject(json, "result"));
      Identifier handle = new Identifier(stack.getSubTag(AltcraftTools.NAMESPACE).getString("handle"));
      return new ShapedHandledRecipe(identifier, group, width, height, ingredients, stack, handle);
    }
  }

}
