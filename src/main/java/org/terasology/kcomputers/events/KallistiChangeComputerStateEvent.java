// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

import javax.annotation.Nonnull;

/**
 * Event sent from the client to the server, used for setting a computer's on/off state.
 */
@ServerEvent
public class KallistiChangeComputerStateEvent implements Event {
    private EntityRef caller = EntityRef.NULL;
    private EntityRef machine = EntityRef.NULL;
    private boolean state;

    public KallistiChangeComputerStateEvent() {

    }

    public KallistiChangeComputerStateEvent(EntityRef machine, EntityRef caller, boolean state) {
        this.machine = machine;
        this.caller = caller;
        this.state = state;
    }

    /**
     * Get the machine which the state change should be applied to.
     *
     * @return The reference to the machine entity.
     */
    @Nonnull
    public EntityRef getMachine() {
        return machine;
    }

    /**
     * Get the entity from which the computer state change call originated, if any.
     *
     * @return The reference to the entity.
     */
    @Nonnull
    public EntityRef getCaller() {
        return caller;
    }

    /**
     * Get the computer's target state.
     *
     * @return True if "on", false if "off".
     */
    public boolean getState() {
        return state;
    }
}
