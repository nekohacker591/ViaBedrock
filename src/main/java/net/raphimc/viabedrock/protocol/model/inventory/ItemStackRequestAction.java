/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.protocol.model.inventory;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType;

/**
 * One action inside an item stack request. The used fields depend on the action type.
 */
public record ItemStackRequestAction(
        ItemStackRequestActionType type,
        Integer amount, // Take/Place/Swap-adjacent/Drop/Destroy/Consume/PlaceInItemContainer/TakeFromItemContainer/ScreenHUDMineBlock
        ItemStackRequestSlot source, // Take/Place/Swap/Drop/Destroy/Consume/PlaceInItemContainer/TakeFromItemContainer/ScreenHUDMineBlock/CraftCreative
        ItemStackRequestSlot destination, // Take/Place/Swap/PlaceInItemContainer/TakeFromItemContainer
        Boolean throwRandomly, // Drop
        String resultItemId, // Create
        Integer primaryEffect, // ScreenBeaconPayment
        Integer secondaryEffect, // ScreenBeaconPayment
        Integer recipeNetId, // CraftRecipe/CraftRecipeAuto/CraftRecipeOptional
        Integer timesCrafted, // CraftRecipeAuto/CraftRecipeOptional
        String filteredString // CraftRecipeOptional
) {

    public static ItemStackRequestAction take(final int amount, final ItemStackRequestSlot source, final ItemStackRequestSlot destination) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Take, amount, source, destination, null, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction place(final int amount, final ItemStackRequestSlot source, final ItemStackRequestSlot destination) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Place, amount, source, destination, null, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction swap(final ItemStackRequestSlot source, final ItemStackRequestSlot destination) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Swap, null, source, destination, null, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction drop(final int amount, final ItemStackRequestSlot source, final boolean throwRandomly) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Drop, amount, source, null, throwRandomly, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction destroy(final int amount, final ItemStackRequestSlot source) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Destroy, amount, source, null, null, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction consume(final int amount, final ItemStackRequestSlot source) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Consume, amount, source, null, null, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction create(final String resultItemId) {
        return new ItemStackRequestAction(ItemStackRequestActionType.Create, null, null, null, null, resultItemId, null, null, null, null, null);
    }

    public static ItemStackRequestAction craftRecipe(final int recipeNetId) {
        return new ItemStackRequestAction(ItemStackRequestActionType.CraftRecipe, null, null, null, null, null, null, null, recipeNetId, null, null);
    }

    public static ItemStackRequestAction craftRecipeAuto(final int recipeNetId, final int timesCrafted) {
        return new ItemStackRequestAction(ItemStackRequestActionType.CraftRecipeAuto, null, null, null, null, null, null, null, recipeNetId, timesCrafted, null);
    }

    public static ItemStackRequestAction craftCreative(final ItemStackRequestSlot source) {
        return new ItemStackRequestAction(ItemStackRequestActionType.CraftCreative, null, source, null, null, null, null, null, null, null, null);
    }

    public static ItemStackRequestAction craftRecipeOptional(final int recipeNetId, final int timesCrafted, final String filteredString) {
        return new ItemStackRequestAction(ItemStackRequestActionType.CraftRecipeOptional, null, null, null, null, null, null, null, recipeNetId, timesCrafted, filteredString);
    }

    public static ItemStackRequestAction screenBeaconPayment(final int primaryEffect, final int secondaryEffect) {
        return new ItemStackRequestAction(ItemStackRequestActionType.ScreenBeaconPayment, null, null, null, null, null, primaryEffect, secondaryEffect, null, null, null);
    }

}
