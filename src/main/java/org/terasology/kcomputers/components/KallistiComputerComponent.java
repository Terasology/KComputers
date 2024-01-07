// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.kallisti.base.component.Machine;

/**
 * Component provided by blocks which hold a Kallisti virtual machine
 * instance.
 */
public class KallistiComputerComponent implements Component<KallistiComputerComponent> {
    public boolean on;
    public transient Machine machine;

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

    @Override
    public void copyFrom(KallistiComputerComponent other) {
        this.on = other.on;
        //FIXME: this is **not** a deep copy, and Machine is a mutable class. only doing a shallow copy of the object reference may lead to
        //       bugs or undesired effects.
        this.machine = other.machine;
    }
}
