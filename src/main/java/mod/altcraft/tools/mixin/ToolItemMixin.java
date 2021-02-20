package mod.altcraft.tools.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.util.Registries;
import net.minecraft.item.Item;
import net.minecraft.item.ToolItem;

@Mixin(ToolItem.class)
public abstract class ToolItemMixin extends Item implements AltcraftHandledItem {
	private Set<Handle> altcraft$specialHandles = Sets.newHashSet();
	private ImmutableList<Handle> altcraft$cachedHandles = null;

	public ToolItemMixin(Settings settings) {
		super(settings);
	}

	public boolean isValidHandle(Handle handle) {
		if (this.altcraft$specialHandles.isEmpty()) {
			return !handle.isSpecial();
		}
		return altcraft$specialHandles.contains(handle);
	}

	public Collection<Handle> getValidHandles() {
		if (!this.altcraft$specialHandles.isEmpty()) {
			return this.altcraft$specialHandles;
		}
		if (this.altcraft$cachedHandles == null) {
			ArrayList<Handle> handleCache = new ArrayList<>(0);
			Registries.HANDLE.stream().filter(this::isValidHandle).forEach(h -> handleCache.add(h));
			this.altcraft$cachedHandles = ImmutableList.copyOf(handleCache);
		}
		return this.altcraft$cachedHandles;
	}

	public boolean addSpecialHandle(Handle handle) {
		return this.altcraft$specialHandles.add(handle);
	}

	public UUID getAttackSpeedModifierUUID() {
		return ATTACK_SPEED_MODIFIER_ID;
	}
}
