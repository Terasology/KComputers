// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.peripherals;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.math.Side;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods useful for peripheral implementations, particularly OpenComputers-emulating ones.
 */
public class PeripheralUtils {
    /**
     * Convert an ItemComponent-containing EntityRef to a Map representation as understood by OpenComputers methods.
     * <p>
     * TODO: This should probably be registered closer to MachineOpenComputers, but necessary APIs haven't been
     * well-designed yet on the Kallisti side. For now, however, this suffices, as all users of it are
     * OpenComputers-specific regardless.
     *
     * @param item The reference to a given item.
     * @return The OpenComputers-friendly Map.
     */
    public static Map<String, Object> convertItemOC(EntityRef item) {
        if (item.exists() && item.hasComponent(ItemComponent.class)) {
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);
            Map<String, Object> map = new HashMap<>();
            map.put("id", itemComponent.stackId);
            map.put("size", itemComponent.stackCount);
            map.put("maxSize", itemComponent.maxStackSize);
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Get the Terasology Side object based on an OpenComputers side parameter.
     *
     * @param side The OC side parameter.
     * @return The matching Terasology side object, or null if invalid.
     */
    @Nullable
    public static Side sideOCToTerasology(int side) {
        switch (side) {
            case 0:
                return Side.BOTTOM;
            case 1:
                return Side.TOP;
            case 2:
                return Side.FRONT;
            case 3:
                return Side.BACK;
            case 4:
                return Side.LEFT;
            case 5:
                return Side.RIGHT;
            default:
                return null;
        }
    }
}
