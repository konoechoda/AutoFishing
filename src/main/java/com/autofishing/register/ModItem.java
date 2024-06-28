package com.autofishing.register;

import com.autofishing.AutoFishing;
import com.autofishing.item.AutoFishingRodItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;


public class ModItem {

    public static final Item AUTO_FISHING_ROD = registerItem("fishing_rod", new AutoFishingRodItem(new Item.Settings().maxDamage(64)));


    @SafeVarargs
    public static Item registerItem(String id, Item item, RegistryKey<ItemGroup>... itemGroups) {
        Item registerItem = Registry.register(Registries.ITEM, new Identifier(AutoFishing.MOD_ID, id), item);
        for (RegistryKey<ItemGroup> itemGroup : itemGroups) {
            ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.add(registerItem));
        }
        return registerItem;
    }

    public static void registerModItems() {

    }
}
