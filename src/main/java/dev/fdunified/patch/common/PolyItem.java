package dev.fdunified.patch.common;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Polymer item overlay adapted from the farmers-delight-patch PolyBaseItem.
 * The knife flag forces server-side handling when a knife is used on a
 * virtual block (e.g. the Farmer's Delight cutting board), suppressing
 * wrong client prediction of the placeholder item on the placeholder block.
 */
public record PolyItem(Item item, boolean knife) implements PolymerItem {
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.TRIAL_KEY;
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return knife || item instanceof BlockItem;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return item instanceof BlockItem;
    }
}
