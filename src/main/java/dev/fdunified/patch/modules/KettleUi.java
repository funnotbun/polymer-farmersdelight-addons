package dev.fdunified.patch.modules;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;

/**
 * SGUI replacement for the kettle screen (same role as the FD patch's
 * CookingPotUi). Built only from vanilla types plus FD's
 * {@link HeatableBlockEntity}: slots come from the live vanilla menu, the
 * title from the MenuProvider, water level from the blockstate, heat from
 * FD's heat check, brew progress from the BE's saved NBT. No compile
 * dependency on Respite itself (matched by class name in the menu mixin).
 */
public class KettleUi extends SimpleGui {
    private final AbstractContainerMenu wrapped;
    private final ServerLevel level;
    private final BlockPos pos;
    private final Slot mealSlot;
    private final Slot containerSlot;
    private int tick;

    public KettleUi(ServerPlayer player, AbstractContainerMenu menu, BlockEntity be, Component title) {
        super(MenuType.GENERIC_9x3, player, false);
        this.wrapped = menu;
        this.level = (ServerLevel) be.getLevel();
        this.pos = be.getBlockPos();
        this.mealSlot = menu.slots.get(2);
        this.containerSlot = menu.slots.get(3);
        this.setTitle(RespiteGui.KETTLE_BACKGROUND.apply(title));
        this.setSlot(2, menu.slots.get(0));
        this.setSlot(2 + 9, menu.slots.get(1));
        this.setSlot(6, new MealDisplay(this.mealSlot, this.containerSlot));
        this.setSlot(5 + 9 * 2, menu.slots.get(3));
        this.setSlot(7 + 9 * 2, menu.slots.get(4));
        this.updateState();
        this.open();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (++this.tick % 10 == 0) {
            this.updateState();
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        this.wrapped.removed(this.player);
    }

    private void updateState() {
        BlockState state = this.level.getBlockState(this.pos);
        int water = 0;
        for (var prop : state.getProperties()) {
            if (prop.getName().equals("water") && prop.getValueClass() == Integer.class) {
                water = state.getValue((net.minecraft.world.level.block.state.properties.IntegerProperty) prop);
            }
        }
        BlockEntity be = this.level.getBlockEntity(this.pos);
        boolean heated = be instanceof HeatableBlockEntity heatable && heatable.isHeated(this.level, this.pos);
        int[] progress = readProgress(be);
        long elapsed = Math.max(0, progress[0]);
        int pct = progress[1] > 0 ? (int) Math.min(100, elapsed * 100 / progress[1]) : 0;
        int arrowPixels = progress[1] > 0
                ? Math.max(0, Math.min(16, (int) (elapsed * 40 / progress[1]) - 22)) : 0;

        this.setSlot(4, RespiteGui.arrowProgress(arrowPixels)
                .setName(Component.literal("Brewing: " + pct + "%").withStyle(ChatFormatting.GRAY)));
        this.setSlot(2 + 9 * 2, heated
                ? RespiteGui.HEATED_ICON.get()
                        .setName(Component.literal("Heated").withStyle(ChatFormatting.GOLD))
                : RespiteGui.EMPTY_ICON.get()
                        .setName(Component.literal("Not heated").withStyle(ChatFormatting.GRAY)));
        this.setSlot(1 + 9 * 2, RespiteGui.waterLevel(water)
                .setName(Component.literal("Water: " + water + "/3").withStyle(ChatFormatting.AQUA)));
    }

    /** CookTime/CookTimeTotal straight from BE NBT (no Respite API needed). */
    private static int[] readProgress(BlockEntity be) {
        try {
            Tag tag = be.saveWithoutMetadata(be.getLevel().registryAccess());
            if (tag instanceof CompoundTag compound) {
                return new int[]{compound.getInt("CookTime").orElse(0),
                        compound.getInt("CookTimeTotal").orElse(0)};
            }
        } catch (RuntimeException ignored) {
            // Fall through to unknown progress; slots stay live regardless.
        }
        return new int[]{0, 0};
    }

    private record MealDisplay(Slot meal, Slot container) implements GuiElement {
        @Override
        public ItemStack getItemStack() {
            var mealStack = this.meal.getItem();
            var b = GuiElementBuilder.from(mealStack);
            b.setName(Component.translatable(mealStack.getItem().getDescriptionId())
                    .withStyle(mealStack.getRarity().color()));
            ItemStack containerStack = this.container.getItem();
            String container = !containerStack.isEmpty()
                    ? Component.translatable(containerStack.getItem().getDescriptionId()).getString() : "";
            b.addLoreLine(Component.literal("Served on " + container).withStyle(ChatFormatting.GRAY));
            b.hideDefaultTooltip();
            b.setMaxCount(99);
            return b.asStack();
        }

        @Override
        public ClickCallback getGuiCallback() {
            return GuiElement.EMPTY_CALLBACK;
        }
    }

}
