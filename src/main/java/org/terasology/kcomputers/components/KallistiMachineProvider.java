// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;

/**
 * Marker interface for components which provide a Kallisti virtual machine instance. Generally, this will be provided
 * in-game as a CPU item.
 */
public interface KallistiMachineProvider {
    /**
     * Create a Kallisti virtual machine.
     *
     * @param kallistiContext The ComponentContext for the KallistiComputerComponent-providing entity.
     * @param computerEntity The KallistiComputerComponent-providing entity.
     * @param providerEntity The KallistiMachineProvider-providing entity.
     * @param memorySize The amount of memory available to the virtual machine, in bytes, if supported.
     * @return A Kallisti virtual machine instance.
     */
    Machine create(ComponentContext kallistiContext, EntityRef computerEntity, EntityRef providerEntity,
                   int memorySize);
}
