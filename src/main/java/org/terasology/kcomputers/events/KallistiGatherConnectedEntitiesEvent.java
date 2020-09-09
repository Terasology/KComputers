// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
     *
     * @param ref The EntityRef.
     */
    public void addEntity(EntityRef ref) {
        if (entities.add(ref)) {
            ref.send(this);
        }
    }

    /**
     * Get a collection of all connected entities.
     *
     * @return The collection.
     */
    public Collection<EntityRef> getEntities() {
        return Collections.unmodifiableSet(entities);
    }
}
