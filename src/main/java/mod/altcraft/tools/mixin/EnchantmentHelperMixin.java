package mod.altcraft.tools.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

  @Redirect(method = "getEnchantments(Ljava/util/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getEnchantability()I"))
  private static int altcraft_modifyEnchantability(Item item, Random random_1, ItemStack stack, int int_1, boolean boolean_1) {
    if (item instanceof AltcraftHandledItem) {
      return Math.round(item.getEnchantability() * Handle.fromItemStack(stack).getEnchantabilityModifier());
    }
    return item.getEnchantability();
  }

  @Redirect(method = "calculateEnchantmentPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getEnchantability()I"))
  private static int altcraft_modifyEnchantability(Item item, Random random_1, int int_1, int int_2, ItemStack stack) {
    if (item instanceof AltcraftHandledItem) {
      return Math.round(item.getEnchantability() * Handle.fromItemStack(stack).getEnchantabilityModifier());
    }
    return item.getEnchantability();
  }

}
