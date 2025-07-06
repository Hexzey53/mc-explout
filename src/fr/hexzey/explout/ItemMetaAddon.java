package fr.hexzey.explout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemMetaAddon {

	private String PackageName;
	private Class<?> CraftItemStack = null;
	private Object nmsItemStack = null;
	private Object tag = null;
	private List<String> CanDestroyList = null;
	private List<String> CanPlaceList = null;
	public ItemMetaAddon(ItemStack item) {
		CanDestroyList = new ArrayList<String>();
		CanPlaceList = new ArrayList<String>();
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			PackageName = "net.minecraft.server." + version;

			//ItemStack(NMS) nmsItemStack = CraftItemStack.asNMSCopy(item);
			CraftItemStack = Class.forName("org.bukkit.craftbukkit."+version+".inventory.CraftItemStack");
			Method asNMSCopy = getDeclaredMethod(CraftItemStack, "asNMSCopy");
			nmsItemStack = asNMSCopy.invoke(null, item);
			//NBTTagCompound tag = nmsItemStack.getTag();
			tag = nmsItemStack.getClass().getDeclaredMethod("getTag").invoke(nmsItemStack, new Object[0]);
			if(tag == null) tag = getNMSClass("NBTTagCompound").newInstance();
			//NBTTagList list$place = tag.getList("CanPlaceOn", 8);
			//NBTTagList list$destroy = tag.getList("CanDestroy", 8);
			Method getList = tag.getClass().getDeclaredMethod("getList", String.class, int.class);
			Object NBTTagList$place = getList.invoke(tag, new Object[]{"CanPlaceOn", 8});
			Object NBTTagList$destroy = getList.invoke(tag, new Object[]{"CanDestroy", 8});
			setList(NBTTagList$place, CanPlaceList);
			setList(NBTTagList$destroy, CanDestroyList);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public List<Material> getCanPlaceMaterial() {
		List<Material> list = new ArrayList<Material>();
		for(String str : CanPlaceList) {
			Material mate = MaterialName.getMaterial(str);
			if(mate != null) list.add(mate);
		}
		return list;
	}
	public List<String> getCanPlaceList() {
		return CanPlaceList;
	}
	public void addCanPlaceMaterial(Material... material) {
		for(Material mate : material) {
			String name = MaterialName.getFullName(mate);
			addCanPlace(name);
		}
	}
	public void addCanPlace(String... args) {
		for(String arg : args) {
			if(!CanPlaceList.contains(arg)) CanPlaceList.add(arg);
		}
	}
	public void setCanPlaceMaterials(List<Material> materials) {
		List<String> list = new ArrayList<String>();
		for(Material mate : materials) {
			String name = MaterialName.getFullName(mate);
			if(!list.contains(name)) list.add(name);
		}
		CanPlaceList = list;
	}
	public void setCanPlace(List<String> args) {
		CanPlaceList = args;
	}
	public List<Material> getCanDestroyMaterial() {
		List<Material> list = new ArrayList<Material>();
		for(String str : CanDestroyList) {
			Material mate = MaterialName.getMaterial(str);
			if(mate != null) list.add(mate);
		}
		return list;
	}
	public List<String> getCanDestroyList() {
		return CanDestroyList;
	}
	public void addCanDestroyMaterial(Material... material) {
		for(Material mate : material) {
			String name = MaterialName.getFullName(mate);
			addCanDestroy(name);
		}
	}
	public void addCanDestroy(String... args) {
		for(String arg : args) {
			if(!CanDestroyList.contains(arg)) CanDestroyList.add(arg);
		}
	}
	public void setCanDestroyMaterials(List<Material> materials) {
		List<String> list = new ArrayList<String>();
		for(Material mate : materials) {
			String name = MaterialName.getFullName(mate);
			if(!list.contains(name)) list.add(name);
		}
		CanDestroyList = list;
	}
	public void setCanDestroy(List<String> args) {
		CanDestroyList = args;
	}
	public ItemStack setItemMetaAddon() {
		try {
			//NBTTagList NBTTagList = new NBTTagList();
			Object NBTTagList$place = getNMSClass("NBTTagList").newInstance();
			Object NBTTagList$destroy = getNMSClass("NBTTagList").newInstance();
			//NBTTagList#add(new NBTTagString(String));
			setNBTTagList(NBTTagList$place, CanPlaceList);
			setNBTTagList(NBTTagList$destroy, CanDestroyList);
			//NBTTagCompound NBTTag = tag.clone();
			Object NBTTag = tag.getClass().getDeclaredMethod("clone").invoke(tag, new Object[0]);
			//NBTTag.set("CanXXX", NBTTagList);
			Method set = getDeclaredMethod(NBTTag.getClass(), "set");
			set.invoke(NBTTag, "CanPlaceOn", NBTTagList$place);
			set.invoke(NBTTag, "CanDestroy", NBTTagList$destroy);
			//nmsItemStack.setTag(tag);
			Method setTag = getDeclaredMethod(nmsItemStack.getClass(), "setTag");
			setTag.invoke(nmsItemStack, NBTTag);
			//ItemStack item = CraftItemStack.asBukkitCopy(nmsItemStack);
			Method asBukkitCopy = getDeclaredMethod(CraftItemStack, "asBukkitCopy");
			Object item = asBukkitCopy.invoke(null, nmsItemStack);
			if(item instanceof ItemStack) return (ItemStack)item;
			else return null;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	private void setList(Object NBTTagList, List<String> list) throws Exception{
		int size = 0;
		Object size_obj = NBTTagList.getClass().getDeclaredMethod("size").invoke(NBTTagList, new Object[0]);
		if(size_obj instanceof Number) size = (int)size_obj;
		Method get = NBTTagList.getClass().getDeclaredMethod("getString", int.class);
		for(int index = 0; index < size; index++) {
			Object str_obj = get.invoke(NBTTagList, index);
			if(str_obj instanceof String) {
				String str = (String)str_obj;
				if(!list.contains(str))
					list.add(str);
			}
		}
	}
	private void setNBTTagList(Object NBTTagList, List<String> list) throws Exception{
		Method add = getDeclaredMethod(NBTTagList.getClass(), "add");
		for(int index = 0; index < list.size(); index++) {
			String arg = list.get(index);

			Constructor<?> NBTTagString_cs = getNMSClass("NBTTagString").getConstructor(new Class<?>[]{String.class});
			Object NBTTagString = NBTTagString_cs.newInstance(arg);
			add.invoke(NBTTagList, NBTTagString);
		}
	}
	private Method getDeclaredMethod(Class<?> source, String name) throws Exception {
		for(Method m : source.getDeclaredMethods()) {
			if(m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}
	private Class<?> getNMSClass(String name) throws Exception {
		return Class.forName(PackageName + "." + name);
	}
	
	private static class MaterialName {
	
		private static String getFullName(Material material) {
			return "minecraft:"+material.name().toLowerCase();
		}
		private static Material getMaterial(String materialName) {
			if(materialName.contains("minecraft:")) {
				materialName = materialName.substring(10);
			}
			return Material.valueOf(materialName.toUpperCase());
		}
	}
		/**
		private static String getIDName(Material material) {
			if(A.containsKey(material)) {
				return "minecraft:"+A.get(material);
			} else {
				return null;
			}
		}
		private static Material getBlockMaterial(String name) {
			if(name.startsWith("minecraft:")) name = name.substring(10);
			if(B.containsKey(name)) {
				return B.get(name);
			} else {
				return null;
			}
		}

		/**
		private static HashMap<Material, String> A = new HashMap<Material, String>();
		private static HashMap<String, Material> B = new HashMap<String, Material>();
		static {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			String PackageName = "net.minecraft.server." + version;
			try {
				setBlockData(PackageName);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		/**
		private static void setBlockData(String PackageName) throws Exception{
			//RegistryBlocks<MinecraftKey, Block> REGISTRY = Block.REGISTRY;
			Object REGISTRY = Class.forName(PackageName + ".Block").getField("REGISTRY").get(null);
			//Set<MinecraftKey> keys = REGISTRY.keySet();
			Object key_obj = REGISTRY.getClass().getMethod("keySet").invoke(REGISTRY, new Object[0]);
			Set<?> keys = key_obj instanceof Set ? (Set<?>)key_obj : new HashSet<Object>();
			// REGISTRY.get REGISTRY.b
			Method get = null;
			Method b = null;
			for(Method m : REGISTRY.getClass().getMethods()) {
				if(m.getName().equals("get")) get = m;
				else if(m.getName().equals("b")) b = m;
			}
			//for(MinecraftKey key : keys) {
			for(Object key : keys) {
				String name = null;
				int id = -1;
				//String name = key.a();
				Object name_obj = key.getClass().getDeclaredMethod("a").invoke(key, new Object[0]);
				if(name_obj instanceof String) name = (String)name_obj;
				//Block block = REGISTRY.get(key);
				//int id = REGISTRY.b(item);
				Object Block = get.invoke(REGISTRY, key);
				Object id_obj = b.invoke(REGISTRY, Block);
				if(id_obj instanceof Number) id = (int)id_obj;
				@SuppressWarnings("deprecation")
				Material mate = Material.getMaterial(id);
				if(name != null && mate != null) {
					A.put(mate, name);
					B.put(name, mate);
				} else {
					name = name == null ? "minecraft:null" : name;
					String mame = mate == null ? "NULL" : mate.name();
					System.out.println(name+" "+mame);
				}
			}
			**/
}