package banduty.bsdfwmi.mixin;

import banduty.bsdfwmi.BsDFWMI;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemEntityRenderer.class)
public class ItemRendererMixin {
    /**
     * @author
     * Banduty
     * @reason
     * Change the rendered amount depend on the max stack
     */
    @Overwrite
    private int getRenderedAmount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > BsDFWMI.CONFIG.common.getMaxGroundStack() * 0.75) {
            i = 5;
        } else if (stack.getCount() > BsDFWMI.CONFIG.common.getMaxGroundStack() * 0.5) {
            i = 4;
        } else if (stack.getCount() > BsDFWMI.CONFIG.common.getMaxGroundStack() * 0.25) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }

        return i;
    }
}
