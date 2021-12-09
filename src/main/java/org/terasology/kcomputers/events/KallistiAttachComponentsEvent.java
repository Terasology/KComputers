// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kcomputers.TerasologyEntityContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Event for attaching Kallisti components to a virtual machine.
 *
 * Send this Event to a given EntityRef to gather all of the components
 * it provides.
 *
 * Receive this Event and use addComponent() to add your own components.
 *
 * @see org.terasology.kcomputers.KComputersUtil
 */
public class KallistiAttachComponentsEvent implements Event {
    private final transient Map<ComponentContext, Object> components = new HashMap<>();
    private final transient Set<Object> addedObjects = Collections.newSetFromMap(new IdentityHashMap<>());
    private int id = 0;

    public KallistiAttachComponentsEvent() {
    }

    /**
     * Add a component to be attached.
     *
     * @param ref The reference to the entity containing the component.
     * @param o The object, which is a Kallisti-compatible component.
     */
    public void addComponent(EntityRef ref, Object o) {
        if (!addedObjects.contains(o)) {
            components.put(new TerasologyEntityContext(ref.getId(), id++), o);
            addedObjects.add(o);
        }
    }

    /**
     * Get the map of component context to their Kallisti objects.
     *
     * @return The map.
     */
    public Map<ComponentContext, Object> getComponentMap() {
        return Collections.unmodifiableMap(components);
    }
}
