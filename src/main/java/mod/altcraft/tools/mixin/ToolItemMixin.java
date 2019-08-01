package mod.altcraft.tools.mixin;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.altcraft.tools.Altcraft;
import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.item.HandleMaterial;
import mod.altcraft.tools.item.HandleMaterials;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(ToolItem.class)
public abstract class ToolItemMixin extends Item implements AltcraftHandledItem {

  public ToolItemMixin(Settings settings) {
    super(settings);
  }

  private static HandleMaterial altcraft_getHandleOrDefault(ItemStack stack, HandleMaterial other) {
    CompoundTag namespace = stack.getSubTag(Altcraft.NAMESPACE);
    if (namespace != null && namespace.containsKey("handle", 8)) {
      return HandleMaterials.getOrDefault(new Identifier(namespace.getString("handle")), other);
    }
    return other;
  }

  private static boolean altcraft_hasCustomHandle(ItemStack stack) {
    CompoundTag namespace = stack.getSubTag(Altcraft.NAMESPACE);
    return namespace != null ? namespace.containsKey("handle", 8) : false;
  }

  @Inject(method = "<init>", at = @At("RETURN"))
  private void altcraft_appendHandlePropertyGetter(ToolMaterial material, Item.Settings settings, CallbackInfo ci) {
    this.addPropertyGetter(new Identifier(Altcraft.NAMESPACE, "handle"), (stack, world, entity) -> ToolItemMixin.altcraft_getHandleOrDefault(stack, HandleMaterials.WOOD).getRawId());
  }

  @Dynamic
  public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext context) {
    super.appendTooltip(stack, world, list, context);
    if (ToolItemMixin.altcraft_hasCustomHandle(stack)) {
      HandleMaterial handle = ToolItemMixin.altcraft_getHandleOrDefault(stack, HandleMaterials.WOOD);
      list.add(new TranslatableText(Altcraft.NAMESPACE + ".item.handle").append(new TranslatableText(handle.getTranslationKey())));
      if (context.isAdvanced()) {
        list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.durability", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getDurabilityModifier()) })).formatted(Formatting.GRAY));
        list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.speed", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getSpeedModifier()) })).formatted(Formatting.GRAY));
        list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.enchantability", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getEnchantabilityModifier()) })).formatted(Formatting.GRAY));
      }
    }
  }
}
