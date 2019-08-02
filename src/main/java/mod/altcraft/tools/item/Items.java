package mod.altcraft.tools.item;

import mod.altcraft.tools.AltcraftTools;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items {

  public static final Item IRON_ROD = new Item(new Item.Settings().group(AltcraftTools.GROUP));
  public static final Item GOLDEN_ROD = new Item(new Item.Settings().group(AltcraftTools.GROUP));

  private static Item register(String string, Item item) {
    return register(new Identifier(AltcraftTools.NAMESPACE, string), item);
  }

  private static Item register(Identifier identifier, Item item) {
    return Registry.register(Registry.ITEM, identifier, item);
  }

  public static void registerItems() {
    register("iron_rod", IRON_ROD);
    register("golden_rod", GOLDEN_ROD);
  }
}
