// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components.parts;

import org.terasology.engine.entitySystem.Component;

/**
 * Component adding memory to a Kallisti virtual machine instance.
 */
public class KallistiMemoryComponent implements Component {
    /**
     * The amount of memory the entity this component is attached to, in bytes.
     */
    public int amount;
}
