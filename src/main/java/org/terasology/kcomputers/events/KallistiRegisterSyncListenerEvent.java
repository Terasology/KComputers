// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

@ServerEvent
public class KallistiRegisterSyncListenerEvent extends NetworkEvent {
    private EntityRef machine;

    public KallistiRegisterSyncListenerEvent() {

    }

    public KallistiRegisterSyncListenerEvent(EntityRef instigator, EntityRef machine) {
        super(instigator);
        this.machine = machine;
    }

    public EntityRef getSyncEntity() {
        return machine;
    }
}
