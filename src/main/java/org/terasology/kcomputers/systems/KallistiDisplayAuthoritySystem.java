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
package org.terasology.kcomputers.systems;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kallisti.base.util.CollectionBackedMultiValueMap;
import org.terasology.kallisti.base.util.MultiValueMap;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.components.KallistiDisplayCandidateComponent;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.components.MeshRenderComponent;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiRegisterSyncListenerEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiDisplayAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private MultiValueMap<EntityRef, EntityRef> displayListeners = new CollectionBackedMultiValueMap<>(new HashMap<>(), HashSet::new);
    private Map<EntityRef, Object> lastSource = new HashMap<>();

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onAttachComponents(KallistiAttachComponentsEvent event, EntityRef ref, KallistiDisplayCandidateComponent component) {
        event.addComponent(ref, component.getDisplay());
    }

    @ReceiveEvent
    public void onAttachComponents(KallistiAttachComponentsEvent event, EntityRef ref, KallistiDisplayComponent component) {
        event.addComponent(ref, component);
    }

    @Override
    public void update(float delta) {
        for (EntityRef machine : displayListeners.keys()) {
            KallistiDisplayComponent displayComponent = machine.getComponent(KallistiDisplayCandidateComponent.class).getDisplay();

            if (displayComponent != null) {
                Object lastSourceObj = lastSource.get(machine);
                Object sourceObj = displayComponent.getSource();

                if (lastSourceObj != sourceObj) {
                    lastSource.put(machine, sourceObj);
                    if (sourceObj != null) {
                        KComputersUtil.synchronize(machine, displayComponent.getSource(), Synchronizable.Type.INITIAL, displayListeners.values(machine));
                    }
                } else {
                    if (sourceObj != null) {
                        KComputersUtil.synchronize(machine, displayComponent.getSource(), Synchronizable.Type.DELTA, displayListeners.values(machine));
                    }
                }
            }
        }
    }

    @ReceiveEvent
    public void displayActivated(OnActivatedComponent event, EntityRef entity, BlockComponent blockComponent, KallistiDisplayCandidateComponent component, MeshRenderComponent meshRenderComponent) {
        if (!component.multiBlock) {
            KallistiDisplayComponent displayComponent = new KallistiDisplayComponent();
            displayComponent.configure(
                entityManager, entity, component, meshRenderComponent
            );
            entity.addComponent(displayComponent);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onRequestInitialUpdate(KallistiRegisterSyncListenerEvent event, EntityRef entity) {
        for (Object o : event.getSyncEntity().iterateComponents()) {
            if (o instanceof KallistiDisplayCandidateComponent) {
                displayListeners.add(event.getSyncEntity(), event.getInstigator());
                lastSource.put(event.getSyncEntity(), null);
            }
        }
    }

    @ReceiveEvent
    public void displayDeactivated(BeforeDeactivateComponent event, EntityRef entity, KallistiDisplayCandidateComponent component) {
        displayListeners.remove(entity);
        lastSource.remove(entity);
    }
}
