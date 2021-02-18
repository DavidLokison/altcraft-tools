package mod.altcraft.tools.client.options;

import org.lwjgl.glfw.GLFW;

import mod.altcraft.tools.AltcraftTools;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class KeyBindings {
	public static FabricKeyBinding MORE_TOOLTIPS;

	static {
		MORE_TOOLTIPS = FabricKeyBinding.Builder.create(new Identifier(AltcraftTools.NAMESPACE, "more_tooltips"), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, "key.category.first.test").build();
		// new KeyBinding("key." + AltcraftTools.NAMESPACE + ".more_tooltips",
		// InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL,
		// "key.category.first.test");
	}

	public static void registerKeyBindings() {
		KeyBindingRegistry.INSTANCE.register(MORE_TOOLTIPS);
		// KeyBindingHelper.registerKeyBinding(MORE_TOOLTIPS);
	}

}
