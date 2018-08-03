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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.components.KallistiDisplayCandidateComponent;
import org.terasology.kcomputers.components.KallistiMachineProvider;
import org.terasology.kcomputers.components.MeshRenderComponent;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiGatherConnectedEntitiesEvent;
import org.terasology.world.block.BlockComponent;

import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiDynamicComponentSystem extends BaseComponentSystem {
    @ReceiveEvent
    public void componentActivated(OnActivatedComponent event, EntityRef entity, BlockComponent component) {
        Map<ComponentContext, Object> contextMap = KComputersUtil.gatherKallistiComponents(entity);
        if (contextMap.isEmpty()) {
            return;
        }

        KallistiGatherConnectedEntitiesEvent gatherEvent = new KallistiGatherConnectedEntitiesEvent();
        gatherEvent.addEntity(entity);

        for (EntityRef ref : gatherEvent.getEntities()) {
            if (ref.hasComponent(KallistiComputerComponent.class)) {
                KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
                Machine machine = computer.getMachine();
                if (machine != null && machine.getState() == Machine.MachineState.RUNNING) {
                    for (Map.Entry<ComponentContext, Object> entry : contextMap.entrySet()) {
                        machine.addComponent(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    @ReceiveEvent
    public void componentDeactivated(BeforeDeactivateComponent event, EntityRef entity, BlockComponent component) {
        Map<ComponentContext, Object> contextMap = KComputersUtil.gatherKallistiComponents(entity);
        if (contextMap.isEmpty()) {
            return;
        }

        KallistiGatherConnectedEntitiesEvent gatherEvent = new KallistiGatherConnectedEntitiesEvent();
        gatherEvent.addEntity(entity);

        for (EntityRef ref : gatherEvent.getEntities()) {
            if (ref.hasComponent(KallistiComputerComponent.class)) {
                KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
                Machine machine = computer.getMachine();
                if (machine != null && machine.getState() == Machine.MachineState.RUNNING) {
                    for (Map.Entry<ComponentContext, Object> entry : contextMap.entrySet()) {
                        machine.removeComponent(entry.getKey());
                    }
                }
            }
        }
    }
}
