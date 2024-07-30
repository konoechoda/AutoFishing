package com.autofishing.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class AutoFishingEnchantment extends Enchantment {

    public AutoFishingEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.FISHING_ROD, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

}
