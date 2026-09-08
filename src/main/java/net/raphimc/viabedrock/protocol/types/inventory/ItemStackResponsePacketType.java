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
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackResponse;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;

public class ItemStackResponsePacketType extends Type<ItemStackResponse> {

    public ItemStackResponsePacketType() {
        super("ItemStackResponse", ItemStackResponse.class);
    }

    @Override
    public ItemStackResponse read(final ByteBuf buffer) {
        final int responsesCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // responses count
        ItemStackResponse first = null;
        for (int i = 0; i < responsesCount; i++) {
            final ItemStackResponse response = readResponse(buffer);
            if (first == null) {
                first = response;
            }
        }
        return first;
    }

    private ItemStackResponse readResponse(final ByteBuf buffer) {
        final int result = Types.BYTE.read(buffer); // result
        final int requestId = Types.VAR_INT.read(buffer); // client request id

        List<ItemStackResponse.Container> containers = null;
        if (result == ItemStackResponse.RESULT_OK) {
            final int containersCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // containers count
            containers = new ArrayList<>(containersCount);
            for (int i = 0; i < containersCount; i++) {
                final FullContainerName containerName = BedrockTypes.FULL_CONTAINER_NAME.read(buffer); // full container name
                final int slotsCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // slots count
                final List<ItemStackResponse.Slot> slots = new ArrayList<>(slotsCount);
                for (int j = 0; j < slotsCount; j++) {
                    final byte requestedSlot = Types.BYTE.read(buffer); // requested slot
                    final byte slot = Types.BYTE.read(buffer); // slot
                    final byte amount = Types.BYTE.read(buffer); // amount
                    final int serverNetId = BedrockTypes.VAR_INT.read(buffer); // item stack net id
                    final String customName = BedrockTypes.STRING.read(buffer); // custom name
                    final String filteredCustomName = BedrockTypes.STRING.read(buffer); // filtered custom name
                    final int durabilityCorrection = BedrockTypes.VAR_INT.read(buffer); // durability correction
                    slots.add(new ItemStackResponse.Slot(requestedSlot, slot, amount, serverNetId, customName, filteredCustomName, durabilityCorrection));
                }
                containers.add(new ItemStackResponse.Container(containerName, slots));
            }
        }

        return new ItemStackResponse(result, requestId, containers);
    }

    @Override
    public void write(final ByteBuf buffer, final ItemStackResponse response) {
        if (response == null) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, 0); // responses count
            return;
        }
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, 1); // responses count
        Types.BYTE.write(buffer, (byte) response.result()); // result
        BedrockTypes.VAR_INT.write(buffer, response.requestId()); // client request id
        if (response.result() == ItemStackResponse.RESULT_OK) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, response.containers() != null ? response.containers().size() : 0); // containers count
            if (response.containers() != null) {
                for (ItemStackResponse.Container container : response.containers()) {
                    BedrockTypes.FULL_CONTAINER_NAME.write(buffer, container.containerName()); // full container name
                    BedrockTypes.UNSIGNED_VAR_INT.write(buffer, container.slots().size()); // slots count
                    for (ItemStackResponse.Slot slot : container.slots()) {
                        Types.BYTE.write(buffer, slot.requestedSlot()); // requested slot
                        Types.BYTE.write(buffer, slot.slot()); // slot
                        Types.BYTE.write(buffer, slot.amount()); // amount
                        BedrockTypes.VAR_INT.write(buffer, slot.serverNetId()); // item stack net id
                        BedrockTypes.STRING.write(buffer, slot.customName()); // custom name
                        BedrockTypes.STRING.write(buffer, slot.filteredCustomName()); // filtered custom name
                        BedrockTypes.VAR_INT.write(buffer, slot.durabilityCorrection()); // durability correction
                    }
                }
            }
        }
    }

}
