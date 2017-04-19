package com.elytradev.teckle.common.tile.base;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.UnitDictionary;
import com.elytradev.probe.api.impl.ProbeData;
import com.elytradev.teckle.client.worldnetwork.DummyNetworkTraveller;
import com.elytradev.teckle.common.TeckleMod;
import com.elytradev.teckle.common.worldnetwork.WorldNetwork;
import com.elytradev.teckle.common.worldnetwork.WorldNetworkNode;
import com.elytradev.teckle.common.worldnetwork.WorldNetworkTraveller;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;

/**
 * Created by darkevilmac on 3/28/2017.
 */
public abstract class TileNetworkMember extends TileEntity {

    @SideOnly(Side.CLIENT)
    public HashMap<NBTTagCompound, DummyNetworkTraveller> travellers;
    public WorldNetworkNode node;
    protected Object probeCapability;

    public TileNetworkMember() {
        if (FMLLaunchHandler.side().isClient())
            travellers = new HashMap<>();
    }

    public void addTraveller(DummyNetworkTraveller traveller) {
        travellers.put(traveller.data, traveller);
    }

    public void removeTraveller(NBTTagCompound data) {
        travellers.remove(data);
    }

    /**
     * Called after a network has been loaded from WorldSavedData, allows adding of missing nodes for endpoints and such.
     *
     * @param network
     */
    public void networkReloaded(WorldNetwork network) {
    }

    /**
     * Check if this tile can be added to a given network with a neighbour on a specified side.
     *
     * @param network the network to add to
     * @param side    the direction of the neighbour that wants to add
     * @return true if can be added false otherwise.
     */
    public boolean isValidNetworkMember(WorldNetwork network, EnumFacing side) {
        return true;
    }

    public WorldNetworkNode getNode() {
        return node;
    }

    public void setNode(WorldNetworkNode node) {
        this.node = node;
    }

    public abstract WorldNetworkNode getNode(WorldNetwork network);

    public abstract boolean canAcceptTraveller(WorldNetworkTraveller traveller, EnumFacing from);

    public abstract boolean canConnectTo(EnumFacing side);

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == null) return null;
        if (capability == TeckleMod.PROBE_CAPABILITY) {
            if (probeCapability == null) probeCapability = new ProbeCapability();
            return (T) probeCapability;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == null) return false;
        if (capability == TeckleMod.PROBE_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    private final class ProbeCapability implements IProbeDataProvider {
        @Override
        public void provideProbeData(List<IProbeData> data) {
            if (node == null)
                return;

            if (TeckleMod.INDEV)
                data.add(new ProbeData(new TextComponentTranslation("tooltip.teckle.node.network", node.network.id.toString().toUpperCase().replaceAll("-", ""))));

            if (!TileNetworkMember.this.getNode().getTravellers().isEmpty()) {
                data.add(new ProbeData(new TextComponentTranslation("tooltip.teckle.traveller.data")));
            }

            for (WorldNetworkTraveller traveller : TileNetworkMember.this.node.getTravellers()) {
                float distance = (Float.valueOf(traveller.activePath.getIndex()) / Float.valueOf(traveller.activePath.pathPositions().size())) * 10F;
                distance += traveller.travelledDistance;
                distance -= 0.1F;
                distance = MathHelper.clamp(distance, 0F, 10F);
                if (distance > 0) {
                    ItemStack stack = new ItemStack(traveller.data.getCompoundTag("stack"));
                    data.add(new ProbeData(new TextComponentString(stack.getDisplayName()))
                            .withInventory(ImmutableList.of(stack))
                            .withBar(0, distance * 10, 100, UnitDictionary.PERCENT));
                }
            }
        }
    }
}
