package mod.altcraft.tools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.handle.Handles;
import mod.altcraft.tools.item.AltcraftHandledItem;
import mod.altcraft.tools.util.Registries;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(ModelLoader.class)
public class ModelLoaderClientMixin {

	@Inject(method = "loadModelFromJson", at = @At("HEAD"), cancellable = true)
	public void altcraft$loadModelFromJson(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> ci) {
		if (id.getPath().startsWith("item/")) {
			Identifier itemId = new Identifier(id.getNamespace(), id.getPath().substring(5));
			Item item = Registry.ITEM.get(itemId);
			if (!(item instanceof AltcraftHandledItem)) {
				return;
			}
			AltcraftTools.LOGGER.debug("Registering Dynamic Model for " + id.toString());
			ci.setReturnValue(altcraft$createBaseModel((AltcraftHandledItem) item, id));
		} else if (id.getPath().startsWith("toolpart/")) {
			AltcraftTools.LOGGER.debug("Registering Toolpart Model for " + id.toString());
			ci.setReturnValue(altcraft$createToolpartModel(id));
		}
	}

	private JsonUnbakedModel altcraft$createBaseModel(AltcraftHandledItem item, Identifier id) {
		String modelJson = "{\n";
		modelJson += "  \"parent\": \"item/handheld\",\n";
		modelJson += "  \"textures\": {\n";
		modelJson += "    \"layer0\": \"" + id.getNamespace() + ":" + id.getPath() + "\"\n";
		modelJson += "  }";
		int minSize = item.getValidHandles().contains(Handles.WOOD) ? 1 : 0;
		if (item.getValidHandles().size() > minSize) {
			modelJson += ",\n  \"overrides\": [";
			for (Handle handle : item.getValidHandles()) {
				if (!(handle == Handles.WOOD)) {
					int handleID = Handle.getRawId(handle);
					String handleName = Registries.HANDLE.getId(handle).getPath();
					modelJson += "{\n    \"predicate\": {\n";
					modelJson += "      \"altcraft:handle\": " + handleID + "\n";
					modelJson += "    },\n";
					modelJson += "    \"model\": \"" + id.getNamespace() + ":toolpart/" + id.getPath().substring(5) + "/" + handleName + "\"\n";
					modelJson += "  }, ";
				}
			}
			modelJson = modelJson.substring(0, modelJson.length() - 2) + "]";
		}
		modelJson += "\n}";
		JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson);
		model.id = id.toString();
		return model;
	}

	private JsonUnbakedModel altcraft$createToolpartModel(Identifier id) {
		String item = id.getPath().substring(9);
		item = item.substring(0, item.indexOf("/")).toString();
		String itemType = item.substring(item.lastIndexOf("_") + 1);
		String handle = id.getPath().substring(id.getPath().indexOf("/", 9) + 1);
		String modelJson = "{\n";
		modelJson += "  \"parent\": \"item/handheld\",\n";
		modelJson += "  \"textures\": {\n";
		modelJson += "    \"layer0\": \"" + id.getNamespace() + ":item/" + item + "\",\n";
		modelJson += "    \"layer1\": \"altcraft:handle/" + itemType + "_" + handle + "\"\n";
		modelJson += "  }\n";
		modelJson += "}";
		JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson);
		model.id = id.toString();
		return model;
	}

}
