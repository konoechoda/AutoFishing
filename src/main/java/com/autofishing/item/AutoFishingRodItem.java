package com.autofishing.item;

import com.autofishing.register.ModItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.Objects;


public class AutoFishingRodItem extends Item implements Vanishable {

    public AutoFishingRodItem(Settings settings) {
        super(settings);
    }

    static class AutoThread extends Thread {
        @Override
        public void run() {
            while (true) {
                ClientPlayerEntity user = MinecraftClient.getInstance().player;
                if (user == null) {
                    return;
                }
                World world = MinecraftClient.getInstance().world;
                if (world == null) {
                    return;
                }
                World serverWorld = Objects.requireNonNull(MinecraftClient.getInstance().getServer()).getWorld(world.getRegistryKey());
                if(serverWorld == null){
                    return;
                }
                PlayerEntity playerEntity = serverWorld.getPlayerByUuid(user.getUuid());
                if(playerEntity == null){
                    return;
                }
                ItemStack itemStackRight = playerEntity.getMainHandStack();
                ItemStack itemStackLeft = playerEntity.getOffHandStack();
                if(itemStackRight.isOf(ModItem.AUTO_FISHING_ROD) || itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                    if (playerEntity.fishHook != null) {
                        FishingBobberEntity bobber = playerEntity.fishHook;
                        try {
                            // it's private field. so I need to use reflection to get it. but it's not recommended.
                            Field hookCountdownFishField = FishingBobberEntity.class.getDeclaredField("hookCountdown");
                            hookCountdownFishField.setAccessible(true);
                            int hookCountdown = hookCountdownFishField.getInt(bobber);

                            if (hookCountdown > 0) {
                                if (itemStackRight.isOf(ModItem.AUTO_FISHING_ROD)) {
                                    itemStackRight.use(world, user, Hand.MAIN_HAND);
                                    itemStackRight.use(serverWorld, playerEntity, Hand.MAIN_HAND);
                                } else if (itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                                    itemStackLeft.use(world, user, Hand.OFF_HAND);
                                    itemStackLeft.use(serverWorld, playerEntity, Hand.OFF_HAND);
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }else{
                    return;
                }
            }
        }

        public boolean isWaiting() {
            return getState() == Thread.State.WAITING || getState() == Thread.State.TIMED_WAITING;
        }
    }

    AutoThread autoThread = new AutoThread();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        ItemStack itemStackRight = user.getMainHandStack();
        ItemStack itemStackLeft = user.getOffHandStack();

        if (user.isAlive()) {

            if(user.fishHook != null) {
                if (itemStackRight.isOf(Items.FISHING_ROD)) {
                    itemStack.use(world, user, Hand.MAIN_HAND);
                } else if (itemStackLeft.isOf(Items.FISHING_ROD)) {
                    itemStack.use(world, user, Hand.OFF_HAND);
                }
            }

            if (itemStackRight.isOf(Items.FISHING_ROD)) {
                itemStack.use(world, user, Hand.MAIN_HAND);
            } else if (itemStackLeft.isOf(Items.FISHING_ROD)) {
                itemStack.use(world, user, Hand.OFF_HAND);
            }

            if (!world.isClient()) {
                return TypedActionResult.success(itemStack, world.isClient());
            }
            if (itemStackRight.isOf(ModItem.AUTO_FISHING_ROD) || itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                if (!autoThread.isAlive()) {
                    autoThread = new AutoThread();
                    autoThread.start();
                } else if (autoThread.isWaiting()) {
                    autoThread.notify();
                }
            } else {
                try {
                    if (autoThread.isAlive())
                        autoThread.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return TypedActionResult.success(itemStack, world.isClient());

    }

    @Override
    public int getEnchantability() {
        return 1;
    }


}
