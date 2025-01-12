package de.srendi.advancedperipherals.common.addons.refinedstorage;

import com.refinedmods.refinedstorage.api.IRSAPI;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.node.NetworkNode;
import dan200.computercraft.shared.util.NBTUtil;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class RefinedStorage {

    private final IRSAPI api;

    public RefinedStorage() {
        this.api = API.instance();
        initiate();
    }

    private static INetworkNode read(CompoundNBT tag, NetworkNode node) {
        node.read(tag);
        return node;
    }

    public static Object[] listFluids(boolean craftable, INetwork network) {
        HashMap<Integer, Object> items = new HashMap<>();
        int i = 1;
        for (FluidStack stack : RefinedStorage.getFluids(network, craftable)) {
            HashMap<String, Object> map = new HashMap<>();
            Set<ResourceLocation> tags = stack.getFluid().getTags();
            ResourceLocation registryName = stack.getFluid().getRegistryName();
            map.put("name", registryName.getPath() + registryName.getNamespace());
            if (craftable) {
                map.put("craftamount", stack.getAmount());
                for (FluidStack oStack : RefinedStorage.getFluids(network, false)) { //Used to get the amount of the item
                    if (oStack.isFluidEqual(stack)) {
                        map.put("amount", oStack.getAmount());
                        break;
                    } else {
                        map.put("amount", 0);
                    }
                }
            } else {
                map.put("amount", stack.getAmount());
            }
            map.put("displayName", stack.getDisplayName().getString());
            if (!tags.isEmpty()) {
                map.put("tags", getListFromTags(tags));
            }
            items.put(i, map);
            i++;
        }
        return new Object[]{items};
    }

    public static Object[] getItem(List<ItemStack> items, ItemStack item) {
        for (ItemStack itemStack : items) {
            if (itemStack.getItem().equals(item.getItem())) {
                return new Object[]{getObjectFromStack(itemStack)};
            }
        }
        return new Object[]{};
    }

    public static Object[] getObjectFromStack(ItemStack itemStack) {
        HashMap<String, Object> map = new HashMap<>();
        CompoundNBT nbt = itemStack.getTag();
        Set<ResourceLocation> tags = itemStack.getItem().getTags();
        ResourceLocation registryName = itemStack.getItem().getRegistryName();
        map.put("name", registryName.getPath() + registryName.getNamespace());
        map.put("amount", itemStack.getCount());
        map.put("displayName", itemStack.getDisplayName().getString());
        if (nbt != null && !nbt.isEmpty()) {
            map.put("nbt", getMapFromNBT(nbt));
        }
        if (!tags.isEmpty()) {
            map.put("tags", getListFromTags(tags));
        }
        return new Object[]{map};
    }

    public static Map<Object, Object> getMapFromNBT(CompoundNBT nbt) {
        Map<Object, Object> map = new HashMap<>();
        for (String value : nbt.keySet()) {
            map.put(value, String.valueOf(nbt.get(value)));
        }
        return map;
    }

    public static List<String> getListFromTags(Set<ResourceLocation> tags) {
        List<String> list = new ArrayList<>();
        for (ResourceLocation value : tags) {
            list.add(value.getNamespace() + ":" + value.getPath());
        }
        return list;
    }

    public static Object[] listItems(boolean craftable, INetwork network) {
        HashMap<Integer, Object> items = new HashMap<>();
        int i = 1;
        for (ItemStack stack : RefinedStorage.getItems(network, craftable)) {
            HashMap<String, Object> map = new HashMap<>();
            CompoundNBT nbt = stack.getTag();
            Set<ResourceLocation> tags = stack.getItem().getTags();
            map.put("name", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            if (craftable)
                map.put("craftamount", stack.getCount()); //Returns the result amount of an crafting recipe
            if (craftable) {
                for (ItemStack oStack : RefinedStorage.getItems(network, false)) { //Used to get the amount of the item
                    if (oStack.isItemEqual(stack)) {
                        map.put("amount", oStack.getCount());
                        break;
                    } else {
                        map.put("amount", 0);
                    }
                }
            } else {
                map.put("amount", stack.getCount());
            }
            map.put("displayName", stack.getDisplayName().getString());
            if (nbt != null && !nbt.isEmpty()) {
                map.put("nbt", getMapFromNBT(nbt));
            }
            if (!tags.isEmpty()) {
                map.put("tags", getListFromTags(tags));
            }
            items.put(i, map);
            i++;
        }
        return new Object[]{items};
    }

    public static List<ItemStack> getItems(INetwork network, boolean craftable) {
        Collection<StackListEntry<ItemStack>> entries;
        if (craftable) {
            entries = network.getItemStorageCache().getCraftablesList().getStacks();
        } else {
            entries = network.getItemStorageCache().getList().getStacks();
        }
        List<ItemStack> result = new ArrayList<>(entries.size());
        for (StackListEntry<ItemStack> entry : entries) {
            result.add(entry.getStack());
        }
        return result;
    }

    public static List<FluidStack> getFluids(INetwork network, boolean craftable) {
        Collection<StackListEntry<FluidStack>> entries;
        if (craftable) {
            entries = network.getFluidStorageCache().getCraftablesList().getStacks();
        } else {
            entries = network.getFluidStorageCache().getList().getStacks();
        }
        List<FluidStack> result = new ArrayList<>(entries.size());
        for (StackListEntry<FluidStack> entry : entries) {
            result.add(entry.getStack());
        }
        return result;
    }

    public static CompoundNBT findMatchingTag(ItemStack stack, String nbtHash, List<ItemStack> items) {
        for (ItemStack rsStack : items) {
            if (rsStack.getCount() > 0 && rsStack.getItem().equals(stack.getItem())) {
                CompoundNBT tag = rsStack.getTag();
                String hash = NBTUtil.getNBTHash(tag);
                AdvancedPeripherals.Debug("HASH: " + hash);
                AdvancedPeripherals.Debug("TAG: " + tag);
                if (nbtHash.equals(hash)) {
                    return tag.copy();
                }
            }
        }
        return null;
    }

    public void initiate() {
        api.getNetworkNodeRegistry().add(new ResourceLocation(AdvancedPeripherals.MOD_ID, "rs_bridge"), (tag, world, pos) -> read(tag, new RefinedStorageNode(world, pos)));
    }

    public IRSAPI getApi() {
        return api;
    }

}
