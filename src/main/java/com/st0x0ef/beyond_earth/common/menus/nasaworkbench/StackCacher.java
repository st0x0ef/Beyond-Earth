package com.st0x0ef.beyond_earth.common.menus.nasaworkbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class StackCacher {

    private final List<Object> list;

    public StackCacher() {
        this.list = new ArrayList<>();
    }

    public boolean test(ItemStack stack) {
        return this.test(Lists.newArrayList(stack));
    }

    public boolean test(FluidStack stack) {
        return this.test(Lists.newArrayList(stack));
    }

    public boolean test(Collection<?> stacks) {
        List<?> list = this.getStacks();
        List<?> tests = new ArrayList<>(stacks);

        int size = list.size();

        if (size != tests.size()) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            Object left = list.get(i);
            Object right = tests.get(i);

            if (!this.test(left, right)) {
                return false;
            }
        }

        return true;
    }

    public boolean test(Object left, Object right) {

        if (left instanceof ItemStack) {
            if (right instanceof ItemStack) {
                return this.test((ItemStack) left, (ItemStack) right);
            }
        } else if (right instanceof FluidStack) {
            return this.test((FluidStack) left, (FluidStack) right);
        }

        return false;
    }

    public boolean test(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        } else return ItemHandlerHelper.canItemStacksStack(left, right);
    }

    public boolean test(FluidStack left, FluidStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        } else return left.getFluid().isSame(right.getFluid()) && FluidStack.areFluidStackTagsEqual(left, right);
    }

    public void set(ItemStack stack) {
        this.set(Lists.newArrayList(stack));
    }

    public void set(FluidStack stack) {
        this.set(Lists.newArrayList(stack));
    }

    public void set(Collection<?> stacks) {
        this.list.clear();

        for (Object object : stacks) {
            if (object instanceof ItemStack) {
                this.list.add(((ItemStack) object).copy());
            } else if (object instanceof FluidStack) {
                this.list.add(((FluidStack) object).copy());
            }
        }
    }

    public List<Object> getStacks() {
        return Collections.unmodifiableList(this.list);
    }
}
