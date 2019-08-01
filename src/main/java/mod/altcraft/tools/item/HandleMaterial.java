package mod.altcraft.tools.item;

import net.minecraft.nbt.StringTag;

public interface HandleMaterial {
  float getDurabilityModifier();

  float getSpeedModifier();

  float getEnchantabilityModifier();

  String getTranslationKey();

  int getRawId();

  StringTag getTag();

}
