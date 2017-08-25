package com.elytradev.teckle.client.render.tile;

import com.elytradev.teckle.client.render.model.ModelMachineOverlay;
import com.elytradev.teckle.common.TeckleObjects;
import com.elytradev.teckle.common.block.BlockRetriever;
import com.elytradev.teckle.common.tile.retriever.TileRetriever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class TileRetrieverRender extends TileEntitySpecialRenderer<TileRetriever> {

    private ModelMachineOverlay mouthOverlay, blinkenLightsOverlay;

    @Override
    public void render(TileRetriever te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState blockState = te.getWorld().getBlockState(te.getPos());
        if (blockState.getBlock() != TeckleObjects.blockRetriever || !te.isLit())
            return;

        EnumFacing pointTo = blockState.getValue(BlockRetriever.FACING);
        if (mouthOverlay == null || blinkenLightsOverlay == null) {
            mouthOverlay = new ModelMachineOverlay(this.getClass(), "teckle:blocks/retrievermouth", false);
            blinkenLightsOverlay = new ModelMachineOverlay(this.getClass(), "teckle:blocks/retrieverblinkenlights", true);
        }
        mouthOverlay.render(getWorld(), new Vec3d(x, y, z), te.getPos(), blockState, pointTo);
        blinkenLightsOverlay.render(getWorld(), new Vec3d(x, y, z), te.getPos(), blockState, pointTo);
    }

}
