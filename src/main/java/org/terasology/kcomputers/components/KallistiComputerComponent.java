// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.component.Machine;

/**
 * Component provided by blocks which hold a Kallisti virtual machine instance.
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
