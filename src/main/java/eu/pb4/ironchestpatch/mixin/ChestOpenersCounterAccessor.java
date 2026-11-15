package eu.pb4.ironchestpatch.mixin;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the chest block entity from vanilla's anonymous opener counter. */
@Mixin(targets = "net.minecraft.world.level.block.entity.ChestBlockEntity$1")
public interface ChestOpenersCounterAccessor {
    @Accessor("this$0")
    ChestBlockEntity polymer$chest();
}
