package com.elytradev.teckle.client.render.model;

import com.elytradev.teckle.common.TeckleLog;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelMachineOverlay {

    private static final ResourceLocation modelLocation = new ResourceLocation("teckle", "block/machineoverlay");
    public static Map<Class<? extends TileEntitySpecialRenderer>, List<ModelMachineOverlay>> overlays = new HashMap<>();
    public static IResourceManagerReloadListener reloadListener = resourceManager -> overlays.values().forEach(l -> l.forEach(ModelMachineOverlay::load));
    private final String textureLocation;
    private final boolean glow;
    private Map<EnumFacing, IBakedModel> models = new HashMap<>();

    public ModelMachineOverlay(Class<? extends TileEntitySpecialRenderer> clazz, String textureLocation, boolean glow) {
        this.textureLocation = textureLocation;
        this.glow = glow;
        overlays.getOrDefault(clazz, new ArrayList<>()).add(this);
        load();
    }

    public void load() {
        models = new HashMap<>();

        try {
            HashMap<EnumFacing, ModelRotation> rotations = new HashMap<>();
            rotations.put(EnumFacing.UP, ModelRotation.X0_Y0);
            rotations.put(EnumFacing.DOWN, ModelRotation.X180_Y0);
            rotations.put(EnumFacing.NORTH, ModelRotation.X90_Y0);
            rotations.put(EnumFacing.SOUTH, ModelRotation.X90_Y180);
            rotations.put(EnumFacing.WEST, ModelRotation.X90_Y270);
            rotations.put(EnumFacing.EAST, ModelRotation.X90_Y90);

            IModel unbakedModel = ModelLoaderRegistry.getModel(modelLocation).retexture(ImmutableMap.of("side", textureLocation));
            //if (glow) {
            //    models.put(EnumFacing.UP, unbakedModel.bake(TRSRTransformation.identity(),
            //            DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
            //}
            for (EnumFacing facing : EnumFacing.VALUES) {
                models.put(facing, unbakedModel.bake(rotations.get(facing),
                        DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
            }
        } catch (Exception e) {
            TeckleLog.error("Failed to load model {} for machine overlay, {}", modelLocation, e);
        }
    }

    public void render(World world, Vec3d vecPos, BlockPos pos, IBlockState state, EnumFacing machineFacing) {
        IBakedModel model = models.get(machineFacing);
        getMC().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (glow) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(vecPos.x, vecPos.y, vecPos.z);
            GlStateManager.pushMatrix();
            // Compensate for a weird bug with renderModelBrightness that results in incorrect transforms.
            GlStateManager.rotate(-90, 0, 1, 0);
            GlStateManager.color(1, 1, 1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            GlStateManager.disableLighting();

            getMC().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightness(model, state, 1, false);
            GlStateManager.popMatrix();

            GlStateManager.enableLighting();
            int light = world.getLight(pos, true);
            int j = light % 65536;
            int k = light / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
            GlStateManager.popMatrix();
        } else {
            RenderHelper.disableStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            GlStateManager.enableRescaleNormal();
            GlStateManager.shadeModel(Minecraft.isAmbientOcclusionEnabled() ? 7425 : 7424);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.setTranslation(vecPos.x, vecPos.y, vecPos.z);
            buffer.begin(7, DefaultVertexFormats.BLOCK);
            getMC().getBlockRendererDispatcher().getBlockModelRenderer().renderModelSmooth(world, model, state, BlockPos.ORIGIN, buffer, false, 0);
            buffer.setTranslation(0, 0, 0);
            tessellator.draw();

            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public Minecraft getMC() {
        return Minecraft.getMinecraft();
    }

}
