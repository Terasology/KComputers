// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.kallisti.base.interfaces.Synchronizable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@OwnerEvent
public class KallistiSyncInitialEvent implements Event {
    private EntityRef entity;
    private byte[] data;

    public KallistiSyncInitialEvent() {

    }

    public KallistiSyncInitialEvent(EntityRef entity, Synchronizable sync) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        sync.writeSyncPacket(Synchronizable.Type.INITIAL, stream);

        this.entity = entity;
        this.data = stream.toByteArray();
    }

    public EntityRef getSyncEntity() {
        return entity;
    }

    public byte[] getData() {
        return data;
    }
}
