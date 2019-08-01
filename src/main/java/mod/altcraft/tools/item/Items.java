package mod.altcraft.tools.item;

import mod.altcraft.tools.Altcraft;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unused")
public class Items {

  public static final Item IRON_ROD;
  public static final Item GOLDEN_ROD;

  private static Item register(Block block, Item item) {
    return register(Registry.BLOCK.getId(block), item);
  }

  private static Item register(String string, Item item) {
    return register(new Identifier(Altcraft.NAMESPACE, string), item);
  }

  private static Item register(Identifier identifier, Item item) {
    if (item instanceof BlockItem) {
      ((BlockItem) item).appendBlocks(Item.BLOCK_ITEMS, item);
    }

    return Registry.register(Registry.ITEM, identifier, item);
  }

  static {
    IRON_ROD = new Item(new Item.Settings().group(Altcraft.GROUP));
    GOLDEN_ROD = new Item(new Item.Settings().group(Altcraft.GROUP));
  }

  public static void registerItems() {
    register("iron_rod", IRON_ROD);
    register("golden_rod", GOLDEN_ROD);
  }
}
