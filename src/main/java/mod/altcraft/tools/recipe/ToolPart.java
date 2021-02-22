package mod.altcraft.tools.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.Identifier;

public class ToolPart implements Predicate<ItemStack> {

	public static final String JSON_IDENTIFIER = AltcraftTools.identifier("toolparts").toString();
	public static final ToolPart NONE = new ToolPart(AltcraftTools.identifier("none"));
	private final Identifier registry;
	private final ItemStack stack;
	private final AltcraftHandledItem item;
	private Ingredient ingredient;

	private ToolPart(Identifier registry, ItemStack stack) {
		this.registry = registry;
		this.stack = stack;
		this.item = (AltcraftHandledItem) stack.getItem();
		this.ingredient = null;
	}

	private ToolPart(Identifier registry) {
		this.registry = registry;
		this.stack = ItemStack.EMPTY;
		this.item = null;
		this.ingredient = null;
	}

	private Ingredient getCachedIngredient() {
		if (this.item == null) {
			return Ingredient.EMPTY;
		}
		if (this.ingredient == null) {
			List<ItemStack> valid = Lists.newArrayList();
			for (Handle handle : this.item.getValidHandles()) {
				valid.addAll(Arrays.asList(handle.getIngredient().getIds().stream().map(RecipeFinder::getStackFromId).toArray(size -> new ItemStack[size])));
			}
			List<Item> items = Lists.newArrayList();
			for (ItemStack stack : valid) {
				if (!(items.contains(stack.getItem()))) {
					items.add(stack.getItem());
				}
			}
			Item[] itemArray = new Item[items.size()];
			for (int i = 0; i < items.size(); ++i) {
				itemArray[i] = items.get(i);
			}
			this.ingredient = Ingredient.ofItems(itemArray);
		}
		return this.ingredient;
	}

	@Override
	public boolean test(ItemStack stack) {
		return getCachedIngredient().test(stack);
	}

	public Ingredient getIngredient() {
		return getCachedIngredient();
	}

	public void addTag(ItemStack output, ItemStack material) {
		if (item == null) {
			return;
		}
		for (Handle handle : this.item.getValidHandles()) {
			if (handle.getIngredient().test(material)) {
				handle.addData(output);
				return;
			}
		}
	}

	public void write(PacketByteBuf buf) {
		buf.writeIdentifier(this.registry);
		buf.writeItemStack(this.stack);
	}

	public static ToolPart fromIdentifier(Identifier registry, ItemStack stack) {
		return new ToolPart(registry, stack);
	}

	public static ToolPart fromPacket(PacketByteBuf buf) {
		Identifier registry = buf.readIdentifier();
		ItemStack stack = buf.readItemStack();
		if (registry.equals(ToolPart.NONE.registry)) {
			return ToolPart.NONE;
		}
		return fromIdentifier(registry, stack);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof ToolPart)) {
			return false;
		} else {
			ToolPart toolpart = (ToolPart) other;
			return this.registry.equals(toolpart.registry) && this.stack.isItemEqualIgnoreDamage(toolpart.stack);
		}
	}

	@Override
	public String toString() {
		return "ToolPart(" + this.registry.toString() + "," + this.stack.getItem().toString() + ")";
	}

	@Override
	public int hashCode() {
		return this.getClass().hashCode() + 12 * this.registry.hashCode() + 5 * this.stack.getItem().hashCode();
	}

}
