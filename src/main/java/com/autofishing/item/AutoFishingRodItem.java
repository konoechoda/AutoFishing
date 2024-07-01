package com.autofishing.item;

import com.autofishing.mixin.FishingBobberEntityMixin;
import com.autofishing.register.ModItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.lang.reflect.Field;


public class AutoFishingRodItem extends Item implements Vanishable {

    public AutoFishingRodItem(Settings settings) {
        super(settings);
    }

    static class AutoThread extends Thread {

        World world;
        PlayerEntity user;

        public AutoThread() {

        }

        public AutoThread(World world, PlayerEntity user) {
            this.world = world;
            this.user = user;
        }

        @Override
        public void run() {
            while (true) {
                if(user == null || world == null){
                    return;
                }
                ItemStack itemStackRight = user.getMainHandStack();
                ItemStack itemStackLeft = user.getOffHandStack();
                if(itemStackRight.isOf(ModItem.AUTO_FISHING_ROD) || itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                    if (user.fishHook != null) {
                        FishingBobberEntity bobber = user.fishHook;
                        try {
                            int hookCountdown =  ((FishingBobberEntityMixin) bobber).getHookCountdown();
                            if (hookCountdown > 0) {
                                for (int i = 0; i < 2; i++) {
                                    if (itemStackRight.isOf(ModItem.AUTO_FISHING_ROD)) {
                                        itemStackRight.use(world, user, Hand.MAIN_HAND);
                                    } else if (itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                                        itemStackLeft.use(world, user, Hand.OFF_HAND);
                                    }
                                    sleep(600); // 0.6 sec sleep time to prevent the hook from hooking the fish in the air
                                }
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    try {
                        sleep(100); // 0.1 sec sleep time to prevent too fast click
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    // if the item is not auto fishing rod, then stop the thread.
                    return;
                }
            }
        }

        public boolean isWaiting() {
            return getState() == Thread.State.WAITING || getState() == Thread.State.TIMED_WAITING;
        }
    }

    AutoThread autoThread = new AutoThread();
    AutoThread autoThreadService = new AutoThread();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        ItemStack itemStackRight = user.getMainHandStack();
        ItemStack itemStackLeft = user.getOffHandStack();

        if (user.isAlive()) {

            if (itemStackRight.isOf(Items.FISHING_ROD)) {
                itemStack.use(world, user, Hand.MAIN_HAND);
            } else if (itemStackLeft.isOf(Items.FISHING_ROD)) {
                itemStack.use(world, user, Hand.OFF_HAND);
            }
            if(!world.isClient()){
                if (itemStackRight.isOf(ModItem.AUTO_FISHING_ROD) || itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                    if (!autoThreadService.isAlive()) {
                        autoThreadService = new AutoThread(world, user);
                        autoThreadService.start();
                    }
                }
            }else {
                if (itemStackRight.isOf(ModItem.AUTO_FISHING_ROD) || itemStackLeft.isOf(ModItem.AUTO_FISHING_ROD)) {
                    if (!autoThread.isAlive()) {
                        autoThread = new AutoThread(world, user);
                        autoThread.start();
                    }
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
