package mod.altcraft.tools.item;

import java.util.Arrays;

import mod.altcraft.tools.Altcraft;
import net.minecraft.item.Item;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public enum HandleMaterials implements HandleMaterial {
  WOOD(0, net.minecraft.item.Items.STICK, 1.0F, 1.0F, 1.0F),
  BONE(1, net.minecraft.item.Items.BONE, 0.7F, 1.2F, 1.3F),
  IRON(2, mod.altcraft.tools.item.Items.IRON_ROD, 1.5F, 0.9F, 0.9F),
  GOLD(3, mod.altcraft.tools.item.Items.GOLDEN_ROD, 0.9F, 0.8F, 1.8F),
  BLAZE(4, net.minecraft.item.Items.BLAZE_ROD, 1.2F, 1.0F, 1.4F),
  BAMBOO(5, net.minecraft.item.Items.BAMBOO, 0.8F, 1.2F, 1.0F);

  private final int rawId;
  private final Identifier identifier;
  private final float durabilityModifier;
  private final float speedModifier;
  private final float enchantabilityModifier;

  private HandleMaterials(int rawId, Item handle, float durabilityModifier, float speedModifier, float enchantabilityModifier) {
    this.rawId = rawId;
    this.identifier = Registry.ITEM.getId(handle);
    this.durabilityModifier = durabilityModifier;
    this.speedModifier = speedModifier;
    this.enchantabilityModifier = enchantabilityModifier;
  }

  public static HandleMaterial getOrDefault(Identifier identifier, HandleMaterial other) {
    return Arrays.asList(HandleMaterials.values()).stream().filter(m -> m.matchesIdentifier(identifier)).map(m -> (HandleMaterial) m).findAny().orElse(other);
  }

  private boolean matchesIdentifier(Identifier identifier) {
    return this.identifier.equals(identifier);
  }

  @Override
  public float getDurabilityModifier() {
    return this.durabilityModifier;
  }

  @Override
  public float getSpeedModifier() {
    return this.speedModifier;
  }

  @Override
  public float getEnchantabilityModifier() {
    return this.enchantabilityModifier;
  }
  
  @Override
  public String getTranslationKey() {
    return "handle." + Altcraft.NAMESPACE + "." + this.name().toLowerCase();
  }
  
  @Override
  public int getRawId() {
    return this.rawId;
  }
  
  @Override
  public StringTag getTag() {
    return new StringTag(this.identifier.toString());
  }
}
