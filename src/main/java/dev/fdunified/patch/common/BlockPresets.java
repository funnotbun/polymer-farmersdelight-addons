package dev.fdunified.patch.common;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModel;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Reusable FactoryBlock presets mirroring the farmers-delight-patch mapping
 * (solid -&gt; BARRIER, feast/pie -&gt; CAMPFIRE, wild crop -&gt; PLANT, crop -&gt; KELP).
 * None of the covered blocks are waterloggable, so only the base variants exist.
 */
public record BlockPresets(BlockState clientState,
                           BiFunction<BlockState, BlockPos, BlockModel> modelFunction) implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {
    public static final BlockPresets BARRIER = new BlockPresets(Blocks.BARRIER.defaultBlockState(), BlockStateModel::longRange);
    public static final BlockPresets CAMPFIRE = new BlockPresets(PolymerBlockResourceUtils.requestEmpty(BlockModelType.CAMPFIRE), BlockStateModel::midRange);
    public static final BlockPresets PLANT = new BlockPresets(PolymerBlockResourceUtils.requestEmpty(BlockModelType.PLANT), BlockStateModel::midRange);
    public static final BlockPresets KELP = new BlockPresets(PolymerBlockResourceUtils.requestEmpty(BlockModelType.KELP), BlockStateModel::midRange);

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return clientState;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return this.modelFunction.apply(initialBlockState, pos);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return false;
    }
}
