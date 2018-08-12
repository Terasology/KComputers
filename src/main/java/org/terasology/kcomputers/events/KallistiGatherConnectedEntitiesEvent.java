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
package org.terasology.kcomputers.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kcomputers.TerasologyEntityContext;
import org.terasology.network.ServerEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Event to gather all EntityRefs connected by Kallisti connections.
 *
 * @see org.terasology.kcomputers.KComputersUtil
 * @see org.terasology.kcomputers.components.KallistiConnectableComponent
 * @see org.terasology.kcomputers.components.KallistiInventoryWithContainerComponent
 */
public class KallistiGatherConnectedEntitiesEvent implements Event {
    private transient final Set<EntityRef> entities = new HashSet<>();

    public KallistiGatherConnectedEntitiesEvent() {
    }

    /**
     * Add a connected entity.
     * @param ref The EntityRef.
     */
    public void addEntity(EntityRef ref) {
        if (entities.add(ref)) {
            ref.send(this);
        }
    }

    /**
     * Get a collection of all connected entities.
     * @return The collection.
     */
    public Collection<EntityRef> getEntities() {
        return Collections.unmodifiableSet(entities);
    }
}
