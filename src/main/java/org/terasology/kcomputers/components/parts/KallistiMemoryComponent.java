// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components.parts;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Component adding memory to a Kallisti virtual machine instance.
 */
public class KallistiMemoryComponent implements Component<KallistiMemoryComponent> {
    /**
     * The amount of memory the entity this component is attached to,
     * in bytes.
     */
    public int amount;

    @Override
    public void copy(KallistiMemoryComponent other) {
        this.amount = other.amount;
    }
}
