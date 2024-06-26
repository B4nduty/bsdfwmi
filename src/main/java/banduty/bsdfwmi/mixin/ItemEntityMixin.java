package banduty.bsdfwmi.mixin;

import banduty.bsdfwmi.BsDFWMI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private static final int maxCount = BsDFWMI.CONFIG.common.getMaxGroundStack();

    /**
     * @author
     * Banduty
     * @reason
     * Change Max Stack Item Entity
     */
    @Overwrite
    private static void merge(ItemEntity targetEntity, ItemStack stack1, ItemStack stack2) {
        ItemStack itemStack = ItemEntity.merge(stack1, stack2, maxCount);
        targetEntity.setStack(itemStack);
    }

    /**
     * @author
     * Banduty
     * @reason
     * Change the canMerge method to work with maxCount
     */
    @Overwrite
    public static boolean canMerge(ItemStack stack1, ItemStack stack2) {
        if (!stack2.isOf(stack1.getItem())) {
            return false;
        } else if (stack2.getCount() + stack1.getCount() > maxCount) {
            return false;
        } else if (stack2.hasNbt() ^ stack1.hasNbt()) {
            return false;
        } else if(stack2.getNbt() == null) return false; return !stack2.hasNbt() || stack2.getNbt().equals(stack1.getNbt());
    }

    @Shadow private int pickupDelay;
    @Shadow private int itemAge;
    @Shadow @Final private static TrackedData<ItemStack> STACK;
    @Shadow public ItemStack getStack() { return this.getDataTracker().get(STACK); }

    /**
     * @author
     * Banduty
     * @reason
     * Change the canMerge method to work with maxCount
     */
    @Overwrite
    private boolean canMerge() {
        ItemStack itemStack = this.getStack();
        return this.isAlive() && this.pickupDelay != 32767 && this.itemAge != -32768 && this.itemAge < 6000 && itemStack.getCount() < maxCount;
    }

    /**
     * @author
     * Banduty
     * @reason
     * Change for Max Value
     */
    @Overwrite
    public static ItemStack merge(ItemStack stack1, ItemStack stack2, int maxCount) {
        int i = Math.min(maxCount - stack1.getCount(), stack2.getCount());
        ItemStack itemStack = stack1.copyWithCount(stack1.getCount() + i);
        stack2.decrement(i);
        return itemStack;
    }

    @Unique
    double distanceItemEntities = BsDFWMI.CONFIG.common.getDistanceItemEntities();

    @ModifyArg(method = "tryMerge()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(DDD)Lnet/minecraft/util/math/Box;"), index = 0)
    private double bsDFWMI$tryMergeX(double x) {
        return distanceItemEntities;
    }

    @ModifyArg(method = "tryMerge()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(DDD)Lnet/minecraft/util/math/Box;"), index = 2)
    private double bsDFWMI$tryMergeZ(double z) {
        return distanceItemEntities;
    }

    @Unique
    private int customTickCounter = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        customTickCounter++;

        MinecraftServer server = this.getWorld().getServer();

        if (server != null) {
            int tps = (int) (server.getAverageTickTime() / 1000);
            if (tps != 0) {
                final int CUSTOM_TICK_RATE = (int) ((20 / Math.pow(tps, 2)) * 400);

                customTickCounter++;
                if (customTickCounter < CUSTOM_TICK_RATE) {
                    ci.cancel();
                } else {
                    customTickCounter = 0;
                }
            }
        }
    }
}