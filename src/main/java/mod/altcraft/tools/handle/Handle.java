package mod.altcraft.tools.handle;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.util.Registries;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class Handle {
	private float durabilityModifier;
	private float speedModifier;
	private float enchantabilityModifier;
	private String translationKey;
	private boolean special;
	private Ingredient ingredient;

	public static int getRawId(Handle handle) {
		return handle == null ? 0 : Registries.HANDLE.getRawId(handle);
	}

	public static Handle byRawId(int id) {
		return Registries.HANDLE.get(id);
	}

	public void addData(ItemStack stack) {
		stack.getOrCreateSubTag(AltcraftTools.NAMESPACE).put("handle", StringTag.of(Registries.HANDLE.getId(this).toString()));
	}

	public Ingredient getIngredient() {
		return this.ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public static Handle fromItemStack(ItemStack stack) {
		CompoundTag namespace = stack.getSubTag(AltcraftTools.NAMESPACE);
		return Registries.HANDLE.get(namespace != null && namespace.contains("handle", 8) ? new Identifier(namespace.getString("handle")) : null);
	}

	public static boolean hasCustomHandle(ItemStack stack) {
		CompoundTag namespace = stack.getSubTag(AltcraftTools.NAMESPACE);
		return namespace != null ? namespace.contains("handle", 8) : false;
	}

	public Handle(Handle.Settings settings) {
		this.durabilityModifier = settings.durabilityModifier;
		this.speedModifier = settings.speedModifier;
		this.enchantabilityModifier = settings.enchantabilityModifier;
		this.special = settings.special;
		this.ingredient = Ingredient.EMPTY;
	}

	public float getDurabilityModifier() {
		return durabilityModifier;
	}

	public float getSpeedModifier() {
		return speedModifier;
	}

	public float getEnchantabilityModifier() {
		return enchantabilityModifier;
	}

	public boolean isSpecial() {
		return this.special;
	}

	public String getTranslationKey() {
		if (this.translationKey == null) {
			this.translationKey = Util.createTranslationKey("handle", Registries.HANDLE.getId(this));
		}
		return this.translationKey;
	}

	public String toString() {
		return Registries.HANDLE.getId(this).getPath();
	}

	public static class Settings {
		private float durabilityModifier = 1.0F;
		private float speedModifier = 1.0F;
		private float enchantabilityModifier = 1.0F;
		private boolean special = false;

		public Handle.Settings durabilityModifier(float durabilityModifier) {
			this.durabilityModifier = durabilityModifier;
			return this;
		}

		public Handle.Settings speedModifier(float speedModifier) {
			this.speedModifier = speedModifier;
			return this;
		}

		public Handle.Settings enchantabilityModifier(float enchantabilityModifier) {
			this.enchantabilityModifier = enchantabilityModifier;
			return this;
		}

		public Handle.Settings special() {
			this.special = true;
			return this;
		}
	}

}
