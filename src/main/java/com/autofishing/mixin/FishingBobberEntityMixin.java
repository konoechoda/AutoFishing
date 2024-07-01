package com.autofishing.mixin;


import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityMixin {

    @Accessor("hookCountdown")
    int getHookCountdown();
}
