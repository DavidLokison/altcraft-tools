package mod.altcraft.tools;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.handle.Handles;
import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.item.Items;
import mod.altcraft.tools.recipe.RecipeSerializers;
import mod.altcraft.tools.recipe.RecipeTypes;
import mod.altcraft.tools.util.Registries;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AltcraftTools implements ModInitializer, ClientModInitializer {
	public static final String NAMESPACE = "altcraft";
	public static final ItemGroup GROUP = FabricItemGroupBuilder.create(new Identifier(NAMESPACE, "tools")).appendItems(stacks -> {
		stacks.add(new ItemStack(Items.IRON_ROD));
		stacks.add(new ItemStack(Items.GOLDEN_ROD));
		Iterator<Item> itemIt = Registry.ITEM.iterator();
		while (itemIt.hasNext()) {
			Item item = itemIt.next();
			if (item instanceof AltcraftHandledItem) {
				AltcraftHandledItem handledItem = (AltcraftHandledItem) item;
				Iterator<Handle> handleIt = handledItem.getValidHandles().iterator();
				while (handleIt.hasNext()) {
					Handle handle = handleIt.next();
					ItemStack stack = new ItemStack(item);
					CompoundTag altcraft = new CompoundTag();
					altcraft.put("handle", StringTag.of(Registries.HANDLE.getId(handle).toString()));
					stack.putSubTag(NAMESPACE, altcraft);
					stacks.add(stack);
				}
			}
		}
	}).icon(() -> {
		ItemStack stack = new ItemStack(net.minecraft.item.Items.IRON_AXE);
		CompoundTag altcraft = new CompoundTag();
		altcraft.put("handle", StringTag.of(Registries.HANDLE.getId(Handles.BLAZE).toString()));
		stack.putSubTag(NAMESPACE, altcraft);
		return stack;
	}).build();
	public static final String LOGGER_PREFIX = "[Altcraft Tools] ";
	public static final Logger LOGGER = LogManager.getLogger(AltcraftTools.class);

	@Override
	public void onInitialize() {
		Registries.registerRegistries();
		Items.registerItems();
		Handles.registerHandles();
		RecipeTypes.registerRecipeTypes();
		RecipeSerializers.registerRecipeSerializers();
	}

	@Override
	public void onInitializeClient() {
		// ModelPredicateProviderRegistrySpecificAccessor
		// KeyBindings.registerKeyBindings();
	}

}
