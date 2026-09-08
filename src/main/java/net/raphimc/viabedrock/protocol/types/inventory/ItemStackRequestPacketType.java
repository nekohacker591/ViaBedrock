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
package net.raphimc.viabedrock.protocol.types.inventory;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequest;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestSlot;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ItemStackRequestPacketType extends Type<ItemStackRequest> {

    public ItemStackRequestPacketType() {
        super("ItemStackRequest", ItemStackRequest.class);
    }

    @Override
    public ItemStackRequest read(final ByteBuf buffer) {
        final int requestId = BedrockTypes.VAR_INT.read(buffer); // client request id
        final int actionsCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // actions count
        final List<ItemStackRequestAction> actions = new ArrayList<>(actionsCount);
        for (int i = 0; i < actionsCount; i++) {
            actions.add(readAction(buffer));
        }
        final int stringsToFilterCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // strings to filter count
        final List<String> stringsToFilter = new ArrayList<>(stringsToFilterCount);
        for (int i = 0; i < stringsToFilterCount; i++) {
            stringsToFilter.add(BedrockTypes.STRING.read(buffer)); // string to filter
        }
        final int stringsToFilterOrigin = BedrockTypes.VAR_INT.read(buffer); // strings to filter origin
        return new ItemStackRequest(requestId, actions, stringsToFilter, stringsToFilterOrigin);
    }

    @Override
    public void write(final ByteBuf buffer, final ItemStackRequest request) {
        BedrockTypes.VAR_INT.write(buffer, request.requestId()); // client request id
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, request.actions().size()); // actions count
        for (ItemStackRequestAction action : request.actions()) {
            writeAction(buffer, action);
        }
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, request.stringsToFilter() != null ? request.stringsToFilter().size() : 0); // strings to filter count
        if (request.stringsToFilter() != null) {
            for (String stringToFilter : request.stringsToFilter()) {
                BedrockTypes.STRING.write(buffer, stringToFilter); // string to filter
            }
        }
        BedrockTypes.VAR_INT.write(buffer, request.stringsToFilterOrigin()); // strings to filter origin
    }

    private ItemStackRequestAction readAction(final ByteBuf buffer) {
        final int actionType = Types.BYTE.read(buffer); // action type
        final net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType type =
                net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType.getByValue(actionType);
        if (type == null) {
            throw new IllegalArgumentException("Unknown item stack request action type: " + actionType);
        }

        Integer amount = null;
        ItemStackRequestSlot source = null;
        ItemStackRequestSlot destination = null;
        Boolean throwRandomly = null;
        String resultItemId = null;
        Integer primaryEffect = null;
        Integer secondaryEffect = null;
        Integer recipeNetId = null;
        Integer timesCrafted = null;
        String filteredString = null;

        switch (type) {
            case Take, Place, PlaceInItemContainer, TakeFromItemContainer -> {
                amount = (int) Types.BYTE.read(buffer); // amount
                source = readSlot(buffer);
                destination = readSlot(buffer);
            }
            case Swap -> {
                source = readSlot(buffer);
                destination = readSlot(buffer);
            }
            case Drop -> {
                amount = (int) Types.BYTE.read(buffer); // amount
                source = readSlot(buffer);
                throwRandomly = Types.BOOLEAN.read(buffer); // throw randomly
            }
            case Destroy, Consume -> {
                amount = (int) Types.BYTE.read(buffer); // amount
                source = readSlot(buffer);
            }
            case Create -> resultItemId = BedrockTypes.STRING.read(buffer); // result item id
            case ScreenBeaconPayment -> {
                primaryEffect = BedrockTypes.VAR_INT.read(buffer); // primary effect
                secondaryEffect = BedrockTypes.VAR_INT.read(buffer); // secondary effect
            }
            case ScreenHUDMineBlock -> {
                amount = BedrockTypes.VAR_INT.read(buffer); // amount
                source = readSlot(buffer);
            }
            case CraftRecipe -> recipeNetId = BedrockTypes.VAR_INT.read(buffer); // recipe net id
            case CraftRecipeAuto -> {
                recipeNetId = BedrockTypes.VAR_INT.read(buffer); // recipe net id
                timesCrafted = (int) Types.BYTE.read(buffer); // times crafted
            }
            case CraftCreative -> source = readSlot(buffer); // creative item net id
            case CraftRecipeOptional -> {
                recipeNetId = BedrockTypes.VAR_INT.read(buffer); // recipe net id
                timesCrafted = (int) Types.BYTE.read(buffer); // times crafted
                filteredString = BedrockTypes.STRING.read(buffer); // filtered string
            }
            case ScreenLabTableCombine, CraftLoom, CraftRepairAndDisenchant, CraftNonImplemented -> {
                // No additional payload
            }
            default -> throw new IllegalArgumentException("Unhandled item stack request action type: " + type);
        }

        return new ItemStackRequestAction(type, amount, source, destination, throwRandomly, resultItemId, primaryEffect, secondaryEffect, recipeNetId, timesCrafted, filteredString);
    }

    private void writeAction(final ByteBuf buffer, final ItemStackRequestAction action) {
        Types.BYTE.write(buffer, (byte) action.type().getValue()); // action type

        switch (action.type()) {
            case Take, Place, PlaceInItemContainer, TakeFromItemContainer -> {
                Types.BYTE.write(buffer, action.amount().byteValue()); // amount
                writeSlot(buffer, action.source());
                writeSlot(buffer, action.destination());
            }
            case Swap -> {
                writeSlot(buffer, action.source());
                writeSlot(buffer, action.destination());
            }
            case Drop -> {
                Types.BYTE.write(buffer, action.amount().byteValue()); // amount
                writeSlot(buffer, action.source());
                Types.BOOLEAN.write(buffer, action.throwRandomly()); // throw randomly
            }
            case Destroy, Consume -> {
                Types.BYTE.write(buffer, action.amount().byteValue()); // amount
                writeSlot(buffer, action.source());
            }
            case Create -> BedrockTypes.STRING.write(buffer, action.resultItemId()); // result item id
            case ScreenBeaconPayment -> {
                BedrockTypes.VAR_INT.write(buffer, action.primaryEffect()); // primary effect
                BedrockTypes.VAR_INT.write(buffer, action.secondaryEffect()); // secondary effect
            }
            case ScreenHUDMineBlock -> {
                BedrockTypes.VAR_INT.write(buffer, action.amount()); // amount
                writeSlot(buffer, action.source());
            }
            case CraftRecipe -> BedrockTypes.VAR_INT.write(buffer, action.recipeNetId()); // recipe net id
            case CraftRecipeAuto -> {
                BedrockTypes.VAR_INT.write(buffer, action.recipeNetId()); // recipe net id
                Types.BYTE.write(buffer, action.timesCrafted().byteValue()); // times crafted
            }
            case CraftCreative -> writeSlot(buffer, action.source()); // creative item net id
            case CraftRecipeOptional -> {
                BedrockTypes.VAR_INT.write(buffer, action.recipeNetId()); // recipe net id
                Types.BYTE.write(buffer, action.timesCrafted().byteValue()); // times crafted
                BedrockTypes.STRING.write(buffer, action.filteredString()); // filtered string
            }
            case ScreenLabTableCombine, CraftLoom, CraftRepairAndDisenchant, CraftNonImplemented -> {
                // No additional payload
            }
            default -> throw new IllegalArgumentException("Unhandled item stack request action type: " + action.type());
        }
    }

    private ItemStackRequestSlot readSlot(final ByteBuf buffer) {
        final FullContainerName containerName = BedrockTypes.FULL_CONTAINER_NAME.read(buffer); // full container name
        final byte slot = Types.BYTE.read(buffer); // slot
        final int netIdVariant = Types.VAR_INT.read(buffer); // net id variant
        if (netIdVariant != 0 && (netIdVariant & 1) != 1) {
            // Malformed variant (stale version flag): treat as no net id rather than trusting the value
            return new ItemStackRequestSlot(containerName, slot, 0);
        }
        return new ItemStackRequestSlot(containerName, slot, netIdVariant >> 1);
    }

    private void writeSlot(final ByteBuf buffer, final ItemStackRequestSlot slot) {
        BedrockTypes.FULL_CONTAINER_NAME.write(buffer, slot.containerName()); // full container name
        Types.BYTE.write(buffer, slot.slot()); // slot
        BedrockTypes.VAR_INT.write(buffer, slot.netIdVariant()); // net id variant
    }

}
