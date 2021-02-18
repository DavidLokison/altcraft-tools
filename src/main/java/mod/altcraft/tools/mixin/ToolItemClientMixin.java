package mod.altcraft.tools.mixin;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

@Mixin(ToolItem.class)
public abstract class ToolItemClientMixin extends Item {

	public ToolItemClientMixin(Settings settings) {
		super(settings);
	}

	@Dynamic
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext context) {
		super.appendTooltip(stack, world, list, context);
		boolean showMore = context.isAdvanced(); // KeyBindings.MORE_TOOLTIPS.isPressed();
		if (Handle.hasCustomHandle(stack)) {
			Handle handle = Handle.fromItemStack(stack);
			list.add(new TranslatableText(AltcraftTools.NAMESPACE + ".item.handle").append(new TranslatableText(handle.getTranslationKey())));
			if (showMore) {
				list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.durability", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getDurabilityModifier()) })).formatted(Formatting.GRAY));
				list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.speed", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getSpeedModifier()) })).formatted(Formatting.GRAY));
				list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.enchantability", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getEnchantabilityModifier()) })).formatted(Formatting.GRAY));
			}
		} else {
			if (((AltcraftHandledItem) this).getValidHandles().size() > 0 && showMore) {
				list.add(new TranslatableText(AltcraftTools.NAMESPACE + ".item.handle.crafting").formatted(Formatting.GRAY));
				for (Handle handle : ((AltcraftHandledItem) this).getValidHandles()) {
					list.add(new LiteralText(" * ").append(new TranslatableText(handle.getTranslationKey())).formatted(Formatting.DARK_GRAY));
				}
			}
		}
	}
}
