package mod.altcraft.tools.util;

import com.mojang.serialization.Lifecycle;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import net.fabricmc.fabric.mixin.registry.sync.AccessorRegistry;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class Registries {

	private static final RegistryKey<Registry<Handle>> HANDLE_KEY;
	public static final DefaultedRegistry<Handle> HANDLE;

	public static void registerRegistries() {
		Registry.register(AccessorRegistry.getROOT(), HANDLE_KEY.getValue(), HANDLE);
	}

	static {
		HANDLE_KEY = RegistryKey.ofRegistry(AltcraftTools.identifier("handle"));
		HANDLE = new DefaultedRegistry<>("altcraft:wood", HANDLE_KEY, Lifecycle.experimental());
	}

}
