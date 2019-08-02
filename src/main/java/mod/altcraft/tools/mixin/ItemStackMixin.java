package mod.altcraft.tools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Multimap;

import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  @Shadow
  public abstract Item getItem();

  @Shadow
  public abstract CompoundTag getSubTag(String name);

  @Inject(at = @At("HEAD"), method = "getMaxDamage", cancellable = true)
  private void altcraft_modifyDurability(CallbackInfoReturnable<Integer> cir) {
    if (this.getItem() instanceof AltcraftHandledItem) {
      cir.setReturnValue(Math.round(this.getItem().getMaxDamage() * Handle.fromItemStack((ItemStack) (Object) this).getDurabilityModifier()));
    }
  }

  @Inject(at = @At("HEAD"), method = "getMiningSpeed", cancellable = true)
  private void altcraft_modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
    if (this.getItem() instanceof AltcraftHandledItem) {
      if (this.getItem().getMiningSpeed((ItemStack) (Object) this, state) != 1.0F) {
        cir.setReturnValue(this.getItem().getMiningSpeed((ItemStack) (Object) this, state) * Handle.fromItemStack((ItemStack) (Object) this).getSpeedModifier());
      }
    }
  }

  @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/Item;getModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"), method = "getAttributeModifiers", locals = LocalCapture.CAPTURE_FAILHARD)
  private void altcraft_modifyAttackSpeed(EquipmentSlot slot, CallbackInfoReturnable<Multimap<String, EntityAttributeModifier>> cir, Multimap<String, EntityAttributeModifier> map) {
    if (this.getItem() instanceof AltcraftHandledItem) {
      if (slot == EquipmentSlot.MAINHAND) {
        EntityAttributeModifier base = map.removeAll(EntityAttributes.ATTACK_SPEED.getId()).iterator().next();
        map.put(EntityAttributes.ATTACK_SPEED.getId(), new EntityAttributeModifier(base.getId(), base.getName(), (4.0 + base.getAmount()) * Handle.fromItemStack((ItemStack) (Object) this).getSpeedModifier() - 4.0, EntityAttributeModifier.Operation.ADDITION));
      }
    }
  }

}
