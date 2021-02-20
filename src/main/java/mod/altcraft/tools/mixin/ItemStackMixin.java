package mod.altcraft.tools.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow
	public abstract Item getItem();

	@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
	private void altcraft$modifyDurability(CallbackInfoReturnable<Integer> cir) {
		if (this.getItem() instanceof AltcraftHandledItem) {
			cir.setReturnValue(Math.round(this.getItem().getMaxDamage() * Handle.fromItemStack((ItemStack) (Object) this).getDurabilityModifier()));
		}
	}

	@Inject(method = "getMiningSpeedMultiplier", at = @At("HEAD"), cancellable = true)
	private void altcraft$modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		if (this.getItem() instanceof AltcraftHandledItem) {
			if (this.getItem().getMiningSpeedMultiplier((ItemStack) (Object) this, state) != 1.0F) {
				cir.setReturnValue(this.getItem().getMiningSpeedMultiplier((ItemStack) (Object) this, state) * Handle.fromItemStack((ItemStack) (Object) this).getSpeedModifier());
			}
		}
	}

	@Redirect(method = "getAttributeModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"))
	private Multimap<EntityAttribute, EntityAttributeModifier> altcraft$Item$getAttributeModifier(Item item, EquipmentSlot slot) {
		if (item instanceof AltcraftHandledItem && slot == EquipmentSlot.MAINHAND) {
			Multimap<EntityAttribute, EntityAttributeModifier> map = item.getAttributeModifiers(slot);
			Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
			for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : map.entries()) {
				if (entry.getKey() == EntityAttributes.GENERIC_ATTACK_SPEED && entry.getValue().getId() == ((AltcraftHandledItem) item).getAttackSpeedModifierUUID()) {
					EntityAttributeModifier value = entry.getValue();
					builder.put(entry.getKey(), new EntityAttributeModifier(value.getId(), value.getName(), (entry.getValue().getValue() + 4.0) * Handle.fromItemStack((ItemStack) (Object) this).getSpeedModifier() - 4.0, value.getOperation()));
				} else {
					builder.put(entry);
				}
			}
			return builder.build();
		} else {
			return item.getAttributeModifiers(slot);
		}
	}

}
