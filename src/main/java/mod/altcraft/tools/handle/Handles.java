package mod.altcraft.tools.handle;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.util.Registries;
import net.minecraft.util.registry.Registry;

public class Handles {

	public static final Handle WOOD = new Handle(new Handle.Settings());
	public static final Handle BONE = new Handle(new Handle.Settings().durabilityModifier(0.7F).speedModifier(1.2F).enchantabilityModifier(1.3F));
	public static final Handle IRON = new Handle(new Handle.Settings().durabilityModifier(1.5F).speedModifier(0.9F).enchantabilityModifier(0.9F));
	public static final Handle GOLD = new Handle(new Handle.Settings().durabilityModifier(0.9F).speedModifier(0.8F).enchantabilityModifier(1.8F));
	public static final Handle BLAZE = new Handle(new Handle.Settings().durabilityModifier(1.2F).enchantabilityModifier(1.4F));
	public static final Handle BAMBOO = new Handle(new Handle.Settings().durabilityModifier(0.8F).speedModifier(1.2F));

	private static Handle register(String string, Handle handle) {
		return Registry.register(Registries.HANDLE, AltcraftTools.NAMESPACE + ":" + string, handle);
	}

	public static void registerHandles() {
		register("wood", WOOD);
		register("bone", BONE);
		register("iron", IRON);
		register("gold", GOLD);
		register("blaze", BLAZE);
		register("bamboo", BAMBOO);
	}

}
