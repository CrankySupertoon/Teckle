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

package com.elytradev.teckle.common.crafting;

import com.elytradev.teckle.common.TeckleObjects;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RecipeSlice extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    public int damageToTool = 1;
    @Nonnull
    protected ItemStack output = ItemStack.EMPTY;
    protected NonNullList<Object> input = NonNullList.create();

    public RecipeSlice(@Nonnull ItemStack result, int damageToTool, Object... recipe) {
        output = result.copy();
        this.damageToTool = damageToTool;
        for (Object in : recipe) {
            if (in instanceof ItemStack) {
                input.add(((ItemStack) in).copy());
            } else if (in instanceof Item) {
                input.add(new ItemStack((Item) in));
            } else if (in instanceof Block) {
                input.add(new ItemStack((Block) in));
            } else if (in instanceof String) {
                input.add(OreDictionary.getOres((String) in));
            } else {
                String ret = "Invalid shapeless ore recipe: ";
                for (Object tmp : recipe) {
                    ret += tmp + ", ";
                }
                ret += output;
                throw new RuntimeException(ret);
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        return output;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    @Nonnull
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
        return output.copy();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     *
     * @param width
     * @param height
     */
    @Override
    public boolean canFit(int width, int height) {
        return (width * height >= (input.size() + 1));
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return !getConsumed(inv).isEmpty();
    }

    private NonNullList getConsumed(InventoryCrafting inv) {
        NonNullList<Object> required = NonNullList.create();
        NonNullList<ItemStack> consumed = NonNullList.create();
        required.addAll(input);
        boolean hasSlicer = false;

        for (int x = 0; x < inv.getSizeInventory(); x++) {
            ItemStack slot = inv.getStackInSlot(x);

            if (!slot.isEmpty()) {
                boolean inRecipe = false;

                if (Objects.equals(slot.getItem(), TeckleObjects.itemBlade)) {
                    if (hasSlicer) {
                        return NonNullList.create();
                    }
                    hasSlicer = true;
                    if (slot.getItemDamage() + damageToTool <= slot.getMaxDamage())
                        inRecipe = true;
                }

                Iterator<Object> req = required.iterator();

                while (req.hasNext()) {
                    boolean match = false;

                    Object next = req.next();

                    if (next instanceof ItemStack) {
                        match = OreDictionary.itemMatches((ItemStack) next, slot, false);
                        if (match) {
                            consumed.add(slot.copy());
                        }
                    } else if (next instanceof List) {
                        Iterator<ItemStack> itr = ((List<ItemStack>) next).iterator();
                        while (itr.hasNext() && !match) {
                            match = OreDictionary.itemMatches(itr.next(), slot, false);
                            if (match) {
                                consumed.add(slot.copy());
                            }
                        }
                    }

                    if (match) {
                        inRecipe = true;
                        required.remove(next);
                        break;
                    }
                }

                if (!inRecipe) {
                    return NonNullList.create();
                }
            }
        }

        if (!required.isEmpty() || !hasSlicer)
            consumed = NonNullList.create();

        return consumed;
    }

    @Override
    @Nonnull
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        InvWrapper wrappedCraftInv = new InvWrapper(inv);

        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        NonNullList<ItemStack> consumed = getConsumed(inv);

        for (int i = 0; i < remaining.size(); i++) {
            remaining.set(i, inv.getStackInSlot(i));
        }

        for (int j = 0; j < consumed.size(); j++) {
            ItemStack consumedStack = consumed.get(j);
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack remainingStack = remaining.get(i).copy();

                if (ItemHandlerHelper.canItemStacksStack(consumedStack, remainingStack)) {
                    ItemStack cStack = wrappedCraftInv.extractItem(i, consumedStack.getCount(), true).copy();
                    consumed.set(j, cStack);

                    if (!cStack.isEmpty()) {
                        remainingStack.setCount(remainingStack.getCount() - cStack.getCount());
                        remaining.set(i, remainingStack);
                    }
                }
            }
        }

        for (int i = 0; i < remaining.size(); i++) {
            if (Objects.equals(remaining.get(i).getItem(), TeckleObjects.itemBlade)) {
                ItemStack remainingBlade = remaining.get(i).copy();
                remainingBlade.attemptDamageItem(damageToTool, new Random(), null);
                if (remainingBlade.getItemDamage() >= remainingBlade.getMaxDamage())
                    remaining.set(i, ItemStack.EMPTY);
                else
                    remaining.set(i, remainingBlade);
                break;
            }
        }

        return remaining;
    }
}
