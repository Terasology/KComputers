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
package org.terasology.kcomputers.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

import javax.annotation.Nonnull;

/**
 * Event sent from the client to the server, used for setting a computer's
 * on/off state.
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
     * @return The reference to the machine entity.
     */
    @Nonnull public EntityRef getMachine() {
        return machine;
    }

    /**
     * Get the entity from which the computer state change call originated, if any.
     * @return The reference to the entity.
     */
    @Nonnull public EntityRef getCaller() {
        return caller;
    }

    /**
     * Get the computer's target state.
     * @return True if "on", false if "off".
     */
    public boolean getState() {
        return state;
    }
}
