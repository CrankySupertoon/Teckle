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

package com.elytradev.teckle.common.tile.sortingmachine.modes.pullmode;

import com.elytradev.teckle.common.TeckleMod;
import com.elytradev.teckle.common.tile.sortingmachine.TileSortingMachine;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class PullModeAuto extends PullMode {
    public int coolDown = 6;

    public PullModeAuto() {
        super(2, 208, 58, "pullmode.auto");
    }

    @Override
    public void onPulse(TileSortingMachine sortingMachine) {
        //NOOP
    }

    @Override
    public void onTick(TileSortingMachine sortingMachine) {
        if (isPaused()) {
            return;
        }

        if (coolDown > 0)
            coolDown--;

        if (coolDown <= 0) {
            sortingMachine.getSortMode().pulse(sortingMachine, this);

            coolDown = TeckleMod.CONFIG.sortingMachineCooldown;
        }
    }

    @Override
    public NBTBase serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger("cooldown", coolDown);
        tagCompound.setBoolean("isPaused", isPaused());

        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        NBTTagCompound tagCompound = (NBTTagCompound) nbt;
        this.coolDown = tagCompound.getInteger("cooldown");

        if (tagCompound.getBoolean("isPaused")) {
            pause();
        } else {
            unpause();
        }
    }
}
