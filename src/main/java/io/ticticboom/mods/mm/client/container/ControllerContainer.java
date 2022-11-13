package io.ticticboom.mods.mm.client.container;

import io.ticticboom.mods.mm.block.entity.ControllerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ControllerContainer extends AbstractContainerMenu {
    private ControllerBlockEntity be;

    protected ControllerContainer(@Nullable MenuType<?> p_38851_, int p_38852_, Inventory inv, ControllerBlockEntity be) {
        super(p_38851_, p_38852_);
        this.be = be;
        for (var i = 0; i < 9; i++) {
            this.addSlot(new Slot(inv, i, i * 18 + 8, 169));
        }

        for (var x = 0; x < 9; x++) {
            for (var y = 0; y < 3; y++) {
                this.addSlot(new Slot(inv, x + y * 9 + 9, x * 18 + 8, y * 18 + 111));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public ControllerContainer(int windowId, Inventory inv, FriendlyByteBuf data, MenuType<?> type) {
        this(type, windowId, inv, (ControllerBlockEntity) inv.player.level.getBlockEntity(data.readBlockPos()));
    }
}