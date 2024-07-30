package com.autofishing.register;

import com.autofishing.AutoFishing;
import com.autofishing.enchantment.AutoFishingEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {

    public static final Enchantment AUTO_FISHING = registerEnchantment("auto_fishing_enchantment", new AutoFishingEnchantment());

    public static Enchantment registerEnchantment(String id, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(AutoFishing.MOD_ID, id), enchantment);
    }

    public static void registerModEnchantments() {
        // 在此调用以确保附魔被注册
    }
}
