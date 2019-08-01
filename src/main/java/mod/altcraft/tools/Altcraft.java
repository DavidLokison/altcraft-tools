package mod.altcraft.tools;

import java.util.Arrays;
import java.util.Iterator;

import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.item.HandleMaterial;
import mod.altcraft.tools.item.HandleMaterials;
import mod.altcraft.tools.item.Items;
import mod.altcraft.tools.recipe.RecipeSerializers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Altcraft implements ModInitializer {
  public static final String NAMESPACE = "altcraft";
  public static final ItemGroup GROUP = FabricItemGroupBuilder.create(new Identifier(NAMESPACE, "tools")).appendItems(stacks -> {
    stacks.add(new ItemStack(Items.IRON_ROD));
    stacks.add(new ItemStack(Items.GOLDEN_ROD));
    Iterator<Item> itemIt = Registry.ITEM.iterator();
    while (itemIt.hasNext()) {
      Item item = itemIt.next();
      if (item instanceof AltcraftHandledItem) {
        Iterator<? extends HandleMaterial> handleIt = Arrays.asList(HandleMaterials.values()).iterator();
        while (handleIt.hasNext()) {
          HandleMaterial handle = handleIt.next();
          ItemStack stack = new ItemStack(item);
          CompoundTag altcraft = new CompoundTag();
          altcraft.put("handle", handle.getTag());
          stack.putSubTag(NAMESPACE, altcraft);
          stacks.add(stack);
        }
      }
    }
  }).icon(() -> {
    ItemStack stack = new ItemStack(net.minecraft.item.Items.IRON_AXE);
    CompoundTag altcraft = new CompoundTag();
    altcraft.put("handle", HandleMaterials.BLAZE.getTag());
    stack.putSubTag(NAMESPACE, altcraft);
    return stack;
  }).build();
  public static final String LOGGERID = "Altcraft";

  @Override
  public void onInitialize() {
    Items.registerItems();
    RecipeSerializers.registerRecipeSerializers();
  }

  /*
   * TODO: test REI Integration
   * TODO: make handled iron (gold), stone (iron, gold) and wood (iron, gold) recipes also available by rods
   * TODO: make German translation working
   */
}
