// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers;

import org.slf4j.Logger;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiSyncDeltaEvent;
import org.terasology.kcomputers.events.KallistiSyncInitialEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Utility methods for KComputers code.
 */
public final class KComputersUtil {
    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("KComputers");

    private KComputersUtil() {

    }

    /**
     * Read an InputStream fully and store it as a byte array.
     *
     * @param stream The InputStream to read.
     * @return The read byte array.
     * @throws IOException Upon issues reading the InputStream.
     */
    public static byte[] toByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        byte[] out = outputStream.toByteArray();
        outputStream.close();
        return out;
    }

    /**
     * Send a synchronization packet for a given target entity to a collection of listening entities.
     * <p>
     * Internal until a synchronization API is formalized and improved.
     *
     * @param target The target entity.
     * @param syncer The Synchronizable instance used to generate the synchronization packet.
     * @param type The type of the synchronization packet.
     * @param listeners The collection of listening entities.
     * @return True if a packet was sent, false otherwise.
     */
    public static boolean synchronize(EntityRef target, Synchronizable syncer, Synchronizable.Type type,
                                      Collection<EntityRef> listeners) {
        try {
            if (!syncer.hasSyncPacket(type)) {
                return false;
            }

            switch (type) {
                case INITIAL:
                    KallistiSyncInitialEvent syncInitial = new KallistiSyncInitialEvent(target, syncer);
                    listeners.forEach((t) -> {
                        if (t.exists()) t.send(syncInitial);
                    });
                    break;
                case DELTA:
                    KallistiSyncDeltaEvent syncDelta = new KallistiSyncDeltaEvent(target, syncer);
                    listeners.forEach((t) -> {
                        if (t.exists()) t.send(syncDelta);
                    });
                    break;
            }

            return true;
        } catch (IOException e) {
            KComputersUtil.LOGGER.warn("Error syncing to client!", e);
            return false;
        }
    }

    /**
     * Small utility method to emit a KallistiAttachComponentsEvent to a given EntityRef and collect its Kallisti
     * components.
     *
     * @param ref The given EntityRef.
     * @return A map of component contexts to their Kallisti objects.
     */
    public static Map<ComponentContext, Object> gatherKallistiComponents(EntityRef ref) {
        KallistiAttachComponentsEvent event = new KallistiAttachComponentsEvent();
        ref.send(event);
        return event.getComponentMap();
    }
}
