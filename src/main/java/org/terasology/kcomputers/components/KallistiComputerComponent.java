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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.component.Machine;

/**
 * Component provided by blocks which hold a Kallisti virtual machine
 * instance.
 */
public class KallistiComputerComponent implements Component {
    private boolean on;
    private transient Machine machine;

    /**
     * Get the Kallisti virtual machine instance held in this component.
     *
     * @return The Kallisti virtual machine instance.
     */
    public Machine getMachine() {
        return machine;
    }

    /**
     * Set the Kallisti virtual machine instance held in this component.
     *
     * @param machine The Kallisti virtual machine instance to be set.
     */
    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    /**
     * This method should be called after, but not necessarily immediately after, setMachine() is used.
     *
     * @param ref The EntityRef containing this component.
     */
    public void onMachineChanged(EntityRef ref) {
        boolean oldOn = on;
        on = machine != null && machine.getState() == Machine.MachineState.RUNNING;
        if (on != oldOn) {
            ref.saveComponent(this);
        }
    }
}
