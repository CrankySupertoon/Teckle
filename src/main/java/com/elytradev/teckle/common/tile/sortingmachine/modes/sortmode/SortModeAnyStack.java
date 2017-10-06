/*
 *    Copyright 2017 Benjamin K (darkevilmac)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.elytradev.teckle.common.tile.sortingmachine.modes.sortmode;

import com.elytradev.teckle.common.tile.inv.SlotData;
import com.elytradev.teckle.common.tile.sortingmachine.NetworkTileSortingMachineBase;
import com.elytradev.teckle.common.tile.sortingmachine.TileSortingMachine;
import com.elytradev.teckle.common.tile.sortingmachine.modes.pullmode.PullMode;
import com.elytradev.teckle.common.worldnetwork.common.WorldNetworkTraveller;
import com.elytradev.teckle.common.worldnetwork.common.node.WorldNetworkEntryPoint;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.stream.Collectors;

public class SortModeAnyStack extends SortMode {
    public SortModeAnyStack(int id, String unlocalizedName, SortModeType type) {
        super(id, unlocalizedName, type);
    }

    public SortModeAnyStack() {
        super(3, "sortmode.anystack", SortModeType.SLOT);
    }

    @Override
    public boolean pulse(TileSortingMachine sortingMachine, PullMode mode) {
        if (sortingMachine.getSource() == null)
            return false;

        List<SlotData> stacksToPush = sortingMachine.getStacksToPush(false);
        if (stacksToPush.isEmpty())
            return false;

        IItemHandler pushStackHandler = sortingMachine.getStacksToPush(false).get(0).itemHandler;
        for (int i = 0; i < stacksToPush.size(); i++) {
            ItemStack stackFromSource = stacksToPush.get(i).getStack();
            if (stackFromSource.isEmpty())
                continue;

            for (int compartmentNumber = 0; compartmentNumber < sortingMachine.getCompartmentHandlers().size(); compartmentNumber++) {
                IItemHandler compartment = sortingMachine.getCompartmentHandlers().get(compartmentNumber);
                EnumDyeColor compartmentColour = sortingMachine.colours[compartmentNumber];

                for (int slot = 0; slot < compartment.getSlots(); slot++) {
                    ItemStack compartmentStack = compartment.getStackInSlot(slot);
                    if (compartmentStack.isEmpty() || !compartmentStack.isItemEqual(stackFromSource))
                        continue;

                    ItemStack result = sortingMachine.addToNetwork(pushStackHandler, i, compartmentStack.getCount(), compartmentColour == null ? ImmutableMap.of()
                            : ImmutableMap.of("colour", new NBTTagInt(compartmentColour.getMetadata())));

                    if (result.isEmpty())
                        return true;
                }
            }

        }

        if (!sortingMachine.defaultRoute.isBlocked()) {
            for (int sourceSlot = 0; sourceSlot < pushStackHandler.getSlots(); sourceSlot++) {
                ItemStack sourceStack = pushStackHandler.getStackInSlot(sourceSlot);
                if (sourceStack.isEmpty())
                    continue;

                ItemStack result = sortingMachine.addToNetwork(pushStackHandler, sourceSlot, sourceStack.getCount(), !sortingMachine.defaultRoute.isColoured() ? ImmutableMap.of()
                        : ImmutableMap.of("colour", new NBTTagInt(sortingMachine.defaultRoute.getMetadata())));
                if (result.isEmpty())
                    return true;
            }
        }

        return false;
    }

    /**
     * Check if the traveller can enter the machine.
     *
     * @param sortingMachine the sorting machine.
     * @param traveller
     * @param from
     * @return
     */
    @Override
    public boolean canAcceptTraveller(NetworkTileSortingMachineBase sortingMachine, WorldNetworkTraveller traveller, EnumFacing from) {
        ItemStack acceptionResult = acceptTraveller(sortingMachine, traveller, true);
        return acceptionResult != null && acceptionResult.isEmpty();
    }

    @Override
    public int selectorPosition(TileSortingMachine sortingMachine) {
        return -1;
    }

    @Override
    public void onTick(TileSortingMachine sortingMachine) {
    }

    /**
     * Accept the given traveller if the machine is set to inline mode.
     *
     * @param sortingMachine
     * @param traveller      the traveller to accept.
     * @param from           the side the traveller is to be injected into.
     * @return true if the entire traveller is accepted, false otherwise.
     */
    @Override
    public ItemStack acceptTraveller(NetworkTileSortingMachineBase sortingMachine, WorldNetworkTraveller traveller, EnumFacing from) {
        return acceptTraveller(sortingMachine, traveller, false);
    }

    private ItemStack acceptTraveller(NetworkTileSortingMachineBase sortingMachine, WorldNetworkTraveller traveller, boolean simulate) {
        if (traveller.data.hasKey("stack")) {
            WorldNetworkTraveller travellerCopy = traveller.clone();
            travellerCopy.data.removeTag("idLeast");
            ItemStack travellerStack = new ItemStack(travellerCopy.data.getCompoundTag("stack"));

            boolean setColour = false;
            for (int compartmentNumber = 0; compartmentNumber < sortingMachine.getCompartmentHandlers().size(); compartmentNumber++) {
                IItemHandler compartment = sortingMachine.getCompartmentHandlers().get(compartmentNumber);
                EnumDyeColor compartmentColour = sortingMachine.getColours()[compartmentNumber];

                for (int slot = 0; slot < compartment.getSlots(); slot++) {
                    ItemStack stackInSlot = compartment.getStackInSlot(slot);
                    if (ItemStack.areItemsEqual(travellerStack, stackInSlot)) {
                        if (compartmentColour != null) {
                            travellerCopy.data.setInteger("colour", compartmentColour.getMetadata());
                        } else {
                            traveller.data.removeTag("colour");
                        }
                        setColour = true;
                        break;
                    }
                    if (setColour)
                        break;
                }
            }

            if (!setColour) {
                if (!sortingMachine.getDefaultRoute().isBlocked()) {
                    if (sortingMachine.getDefaultRoute().isColoured()) {
                        travellerCopy.data.setInteger("colour", sortingMachine.getDefaultRoute().getColour().getMetadata());
                    } else {
                        travellerCopy.data.removeTag("colour");
                    }
                    setColour = true;
                }
            }

            if (setColour) {
                BlockPos insertInto = sortingMachine.getPos().offset(sortingMachine.getOutputTile().getOutputFace());
                ImmutableMap<String, NBTBase> collect = ImmutableMap.copyOf(travellerCopy.data.getKeySet().stream().collect(Collectors.toMap(o -> o, o -> travellerCopy.data.getTag(o))));
                ItemStack result = sortingMachine.getNetworkAssistant(ItemStack.class).insertData((WorldNetworkEntryPoint) sortingMachine.getOutputTile().getNode(),
                        insertInto, travellerStack, collect, false, simulate);
                if (!result.isEmpty() && !simulate) {
                    if (result.getCount() != travellerStack.getCount())
                        sortingMachine.setTriggered();
                    traveller.data.setTag("stack", result.serializeNBT());
                } else if (!simulate) {
                    sortingMachine.setTriggered();
                }

                return result;
            }
        }

        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        // We don't store anything of interest.
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        // We don't store anything of interest.
    }
}
