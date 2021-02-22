package mod.altcraft.tools.recipe;

import java.util.Map;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface ToolPartRecipe {

	public Map<ToolPart, ItemStack> getToolpartMaterials(CraftingInventory inventory);

	public void setToolparts(DefaultedList<ToolPart> toolparts);

	public DefaultedList<ToolPart> getToolparts();

}
