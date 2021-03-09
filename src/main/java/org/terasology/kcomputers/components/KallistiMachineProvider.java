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
package org.terasology.kcomputers.components;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;

/**
 * Marker interface for components which provide a Kallisti virtual machine
 * instance. Generally, this will be provided in-game as a CPU item.
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
    Machine create(ComponentContext kallistiContext, EntityRef computerEntity, EntityRef providerEntity, int memorySize);
}
