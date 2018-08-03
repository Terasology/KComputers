/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.kcomputers.peripherals;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PeripheralUtils {
    public static Map<String, Object> convertItem(EntityRef item) {
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
}
