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

package com.elytradev.teckle.api.capabilities;

import com.elytradev.teckle.common.worldnetwork.common.node.WorldNetworkEntryPoint;
import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Primarily used to allow mods to interact with worldnetworks.
 * Whether that be hooks for node registration and detection or adding travellers to networks.
 *
 * @param <T>
 */
public interface IWorldNetworkAssistant<T extends INBTSerializable> {

    void onNodePlaced(World world, BlockPos pos);

    void onNodeNeighbourChange(World world, BlockPos pos, BlockPos neighbourPos);

    void onNodeBroken(World world, BlockPos pos);

    /**
     * Inserts the given data into a network
     *
     * @param entryPoint the entry point node this data is coming from.
     * @param insertInto the position we're trying to insert into.
     * @param insertData the data we're inserting.
     * @return the remaining data that was not inserted.
     */
    T insertData(WorldNetworkEntryPoint entryPoint, BlockPos insertInto, T insertData, ImmutableMap<String, NBTBase> additionalData);

}
