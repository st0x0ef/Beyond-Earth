package com.st0x0ef.beyond_earth.common.data.recipes;

import com.google.gson.JsonObject;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.st0x0ef.beyond_earth.common.registries.RecipeSerializersRegistry;
import com.st0x0ef.beyond_earth.common.registries.RecipeTypeRegistry;

public class OxygenBubbleDistributorRecipe extends OxygenMakingRecipeAbstract {

	public OxygenBubbleDistributorRecipe(ResourceLocation id, JsonObject json) {
		super(id, json);
	}

	public OxygenBubbleDistributorRecipe(ResourceLocation id, FriendlyByteBuf buffer) {
		super(id, buffer);
	}

	public OxygenBubbleDistributorRecipe(ResourceLocation id, FluidIngredient ingredient, int oxygen) {
		super(id, ingredient, oxygen, 0);
	}

	@Override
	public boolean matches(Container container, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess access) {
		return null;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess access) {
		return null;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializersRegistry.RECIPE_SERIALIZER_OXYGEN_BUBBLE_DISTRIBUTOR.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeTypeRegistry.OXYGEN_BUBBLE_DISTRIBUTING.get();
	}

}
