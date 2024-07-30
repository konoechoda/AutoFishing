package com.autofishing.mixin;

import com.autofishing.register.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin{

    Map<UUID, AutoThread> autoThread = new HashMap<>();
    Map<UUID, AutoThread> autoThreadService = new HashMap<>();

    static class AutoThread extends Thread {

        World world;
        PlayerEntity user;
        Hand hand;
        int level;

        public AutoThread(World world, PlayerEntity user, Hand hand, int level) {
            this.world = world;
            this.user = user;
            this.hand = hand;
            this.level = Math.min(level, 2);
        }

        @Override
        public void run() {
            while(user.fishHook != null) {
                if(!user.isAlive()){
                    return;
                }
                FishingBobberEntity bobber = user.fishHook;
                int hookCountdown =  ((FishingBobberEntityMixin) bobber).getHookCountdown();
                if(hookCountdown > 0) {
                    ItemStack itemStack = user.getStackInHand(hand);
                    for (int i = 0; i < level; i++) {
                        itemStack.use(world, user, hand);
                    }
                    try {
                        sleep(600); // 0.6 sec sleep time to prevent the hook from hooking the fish in the air
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    sleep(100); // 0.1 sec sleep time to prevent too fast click
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
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
                world.spawnEntity(new FishingBobberEntity(user, world, j, i));
            }
            user.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }

        int level = EnchantmentHelper.getLevel(ModEnchantments.AUTO_FISHING, itemStack);
        if(level > 0) {
            AutoThread threadService = autoThreadService.get(user.getUuid());
            AutoThread thread = autoThread.get(user.getUuid());
            if(!world.isClient){
                if(threadService == null || !threadService.isAlive()){
                    threadService = new AutoThread(world, user, hand, level);
                    autoThreadService.put(user.getUuid(), threadService);
                    threadService.start();
                }
            }else{
                if(thread == null || !thread.isAlive()){
                    thread = new AutoThread(world, user, hand, level);
                    autoThread.put(user.getUuid(), thread);
                    thread.start();
                }
            }
        }

        cir.setReturnValue(TypedActionResult.success(itemStack, world.isClient()));
        cir.cancel();
    }
}