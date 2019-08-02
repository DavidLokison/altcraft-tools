package mod.altcraft.tools.item;

import java.util.Collection;

import mod.altcraft.tools.handle.Handle;

public interface AltcraftHandledItem {
  public boolean isValidHandle(Handle handle);
  
  public Collection<Handle> getValidHandles();

  public boolean addSpecialHandle(Handle handle);

}
