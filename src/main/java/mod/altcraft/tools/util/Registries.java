package mod.altcraft.tools.util;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public class Registries {
  
  public static final DefaultedRegistry<Handle> HANDLE = new DefaultedRegistry<>("altcraft:wood");
  
  public static void registerRegistries() {
    Registry.register(Registry.REGISTRIES, new Identifier(AltcraftTools.NAMESPACE, "handle"), HANDLE);
  }

}
