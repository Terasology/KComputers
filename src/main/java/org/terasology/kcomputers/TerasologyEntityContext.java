// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers;

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.interfaces.ConnectedContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A ComponentContext implementation allowing unique identification of
 * Terasology entities by Kallisti's environment.
 *
 * TODO: Better ways of discerning multiple components in one entity
 * should be added. The incrementing "id" field is a stopgap measure.
 *
 * @see org.terasology.kallisti.base.component.ComponentContext
 */
public class TerasologyEntityContext implements ComponentContext, ConnectedContext {
    private final long entityId;
    private final int id;
    private final List<ComponentContext> neighbors = new ArrayList<>();

    /**
     * Create a new Terasology entity context
     *
     * @param entityId The entity ID
     * @param id A value used for discerning between multiple components in one entity.
     */
    public TerasologyEntityContext(long entityId, int id) {
        this.entityId = entityId;
        this.id = id;
    }

    /**
     * Get the entity ID behind a given component context.
     *
     * @return The entity ID.
     */
    public long getEntityId() {
        return entityId;
    }

    public void addNeighbor(TerasologyEntityContext context) {
        neighbors.add(context);
    }

    @Override
    public String identifier() {
        return entityId + ":" + id;
    }

    @Override
    public List<ComponentContext> getNeighbors() {
        return neighbors;
    }
}
