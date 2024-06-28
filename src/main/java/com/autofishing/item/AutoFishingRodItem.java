package com.autofishing.item;

import com.autofishing.register.ModItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class AutoFishingRodItem extends Item implements Vanishable {

    public AutoFishingRodItem(Settings settings) {
        super(settings);
    }

    static class AutoThread extends Thread {
        @Override
        public void run() {
            while (true) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player == null) {
                    return;
                }
                if(player.fishHook != null) {
                    if (player.fishHook.getHookedEntity() != null) {
                        ItemStack itemStack = player.getMainHandStack();
                        boolean bl = itemStack.isOf(ModItem.AUTO_FISHING_ROD);
                        if (bl) {
                            // 模拟玩家右键点击行为
                            TypedActionResult<ItemStack> result = itemStack.use(player.getWorld(), player, Hand.MAIN_HAND);
                        }
                    }
                }
            }
        }
    }

    AutoThread autoThread = new AutoThread();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.fishHook != null) {
            if (!world.isClient) {
                int i = user.fishHook.use(itemStack);
                itemStack.damage(i, user, p -> p.sendToolBreakStatus(hand));
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            if (!world.isClient) {
                int i = EnchantmentHelper.getLure(itemStack);
                int j = EnchantmentHelper.getLuckOfTheSea(itemStack);
                FishingBobberEntity fishingBobberEntity = new FishingBobberEntity(user, world, j, i);
                world.spawnEntity(fishingBobberEntity);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }

        ItemStack itemStackRight = user.getMainHandStack();
        boolean bl = itemStackRight.isOf(ModItem.AUTO_FISHING_ROD);
        if (bl) {
            if (!world.isClient) {
                if(!autoThread.isAlive()){
                    autoThread = new AutoThread();
                    autoThread.start();
                }
            }
        }else {
            try {
                if(autoThread.isAlive())
                    autoThread.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public int getEnchantability() {
        return 1;
    }


}
