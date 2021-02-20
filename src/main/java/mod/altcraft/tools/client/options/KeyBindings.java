package mod.altcraft.tools.client.options;

import org.lwjgl.glfw.GLFW;

import mod.altcraft.tools.AltcraftTools;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class KeyBindings {
	public static KeyBinding MORE_TOOLTIPS;

	static {
		MORE_TOOLTIPS = new KeyBinding("key." + AltcraftTools.NAMESPACE + ".more_tooltips", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, "key.category.first.test");
	}

	public static void registerKeyBindings() {
		KeyBindingHelper.registerKeyBinding(MORE_TOOLTIPS);
	}

}
