package mod.altcraft.tools.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Sets;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.util.Registries;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(ToolItem.class)
public abstract class ToolItemMixin extends Item implements AltcraftHandledItem {
  private Set<Handle> specialHandles = Sets.newHashSet();
  
  public ToolItemMixin(Settings settings) {
    super(settings);
  }
  
  public boolean isValidHandle(Handle handle) {
    if (this.specialHandles.isEmpty()) {
      return !handle.isSpecial();
    }
    return specialHandles.contains(handle);
  }
  
  public Collection<Handle> getValidHandles() {
    if (!this.specialHandles.isEmpty()) {
      return this.specialHandles;
    }
    return Registries.HANDLE.stream().filter(this::isValidHandle).collect(Sets::newHashSet, Set::add, Set::addAll);
  }
  
  public boolean addSpecialHandle(Handle handle) {
    return this.specialHandles.add(handle);
  }

  @Inject(method = "<init>", at = @At("RETURN"))
  private void altcraft_appendHandlePropertyGetter(ToolMaterial material, Item.Settings settings, CallbackInfo ci) {
    this.addPropertyGetter(new Identifier(AltcraftTools.NAMESPACE, "handle"), (stack, world, entity) -> Handle.getRawId(Handle.fromItemStack(stack)));
  }

  @Dynamic
  public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext context) {
    super.appendTooltip(stack, world, list, context);
    if (Handle.hasCustomHandle(stack)) {
      Handle handle = Handle.fromItemStack(stack);
      list.add(new TranslatableText(AltcraftTools.NAMESPACE + ".item.handle").append(new TranslatableText(handle.getTranslationKey())));
      if (context.isAdvanced()) {
        list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.durability", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getDurabilityModifier()) })).formatted(Formatting.GRAY));
        list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.speed", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getSpeedModifier()) })).formatted(Formatting.GRAY));
        list.add(new LiteralText(" ").append(new TranslatableText("altcraft.handle.enchantability", new Object[] { ItemStack.MODIFIER_FORMAT.format(handle.getEnchantabilityModifier()) })).formatted(Formatting.GRAY));
      }
    }
  }
}
