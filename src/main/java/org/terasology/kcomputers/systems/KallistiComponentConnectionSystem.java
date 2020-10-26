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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.TerasologyEntityContext;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.events.KallistiGatherConnectedEntitiesEvent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This system handles dynamic addition and removal of Kallisti components to
 * Kallisti machines in the Terasology world space.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiComponentConnectionSystem extends BaseComponentSystem {
    @In
    private WorldProvider provider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

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

    private void removeNoLongerVisibleComponents(EntityRef entity) {
        KallistiGatherConnectedEntitiesEvent gatherEvent = new KallistiGatherConnectedEntitiesEvent();
        gatherEvent.addEntity(entity);

        TLongSet entityIds = new TLongHashSet();
        for (EntityRef ref : gatherEvent.getEntities()) {
            if (ref.exists()) {
                entityIds.add(ref.getId());
            }
        }

        for (EntityRef ref : gatherEvent.getEntities()) {
            if (ref.hasComponent(KallistiComputerComponent.class)) {
                KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
                Machine machine = computer.getMachine();
                if (machine != null && machine.getState() == Machine.MachineState.RUNNING) {
                    List<ComponentContext> contextsToRemove = new ArrayList<>();

                    for (ComponentContext c : machine.getAllComponentContexts()) {
                        if (c instanceof TerasologyEntityContext && !entityIds.contains(((TerasologyEntityContext) c).getEntityId())) {
                            contextsToRemove.add(c);
                        }
                    }

                    for (ComponentContext c : contextsToRemove) {
                        machine.removeComponent(c);
                    }
                }
            }
        }
    }

    @ReceiveEvent
    public void componentDeactivated(BeforeDeactivateComponent event, EntityRef entity, BlockComponent component) {
        removeNoLongerVisibleComponents(entity);

        for (Side side : Side.values()) {
            Vector3i pos = side.getAdjacentPos(component.getPosition());
            if (pos != null && provider.isBlockRelevant(pos) && blockEntityRegistry.hasPermanentBlockEntity(pos)) {
                EntityRef ref = blockEntityRegistry.getBlockEntityAt(pos);
                if (ref != null && ref.exists()) {
                    removeNoLongerVisibleComponents(ref);
                }
            }
        }
    }
}
