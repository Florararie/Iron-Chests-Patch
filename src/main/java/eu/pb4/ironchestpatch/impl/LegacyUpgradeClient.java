package eu.pb4.ironchestpatch.impl;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/** Client representation of a pre-3.0 {@code ironchest:*_upgrade} placeholder stack. */
public final class LegacyUpgradeClient {
    public static final PolyCoreItem INSTANCE = new PolyCoreItem(
            Items.PAPER,
            "Legacy Chest Upgrade",
            List.of(Component.literal("Converts on pickup").withStyle(ChatFormatting.GRAY)));

    private LegacyUpgradeClient() {}
}
