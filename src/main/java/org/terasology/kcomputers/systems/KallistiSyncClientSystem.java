// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.events.KallistiSyncDeltaEvent;
import org.terasology.kcomputers.events.KallistiSyncInitialEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This system handles receiving KallistiSync*Events and forwarding them to the correct receiver.
 *
 * @see KallistiSyncDeltaEvent
 * @see KallistiSyncInitialEvent
 */
@RegisterSystem(RegisterMode.CLIENT)
public class KallistiSyncClientSystem extends BaseComponentSystem {
    @ReceiveEvent
    public void onSyncInitial(KallistiSyncInitialEvent event, EntityRef entity) {
        onSync(event.getSyncEntity(), event.getData(), Synchronizable.Type.INITIAL);
    }

    @ReceiveEvent
    public void onSyncDelta(KallistiSyncDeltaEvent event, EntityRef entity) {
        onSync(event.getSyncEntity(), event.getData(), Synchronizable.Type.DELTA);
    }

    private void onSync(EntityRef target, byte[] data, Synchronizable.Type type) {
        Synchronizable.Receiver s = null;

        for (Object o : KComputersUtil.gatherKallistiComponents(target).values()) {
            if (o instanceof Synchronizable.Receiver) {
                if (s != null) {
                    throw new RuntimeException("May only have one Synchronizable per Entity! TODO");
                } else {
                    s = (Synchronizable.Receiver) o;
                }
            }
        }

        if (s != null) {
            try {
                s.update(new ByteArrayInputStream(data));
            } catch (IOException e) {
                KComputersUtil.LOGGER.warn("Error syncing with client!", e);
            }
        } else {
            // TODO: log warning
        }
    }
}
