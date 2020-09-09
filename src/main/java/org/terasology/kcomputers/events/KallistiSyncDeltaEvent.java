// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.kallisti.base.interfaces.Synchronizable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@BroadcastEvent
public class KallistiSyncDeltaEvent implements Event {
    private EntityRef entity;
    private byte[] data;

    public KallistiSyncDeltaEvent() {

    }

    public KallistiSyncDeltaEvent(EntityRef entity, Synchronizable sync) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        sync.writeSyncPacket(Synchronizable.Type.DELTA, stream);

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
