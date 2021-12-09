// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.systems;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.TerasologyEntityContext;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.events.KallistiGatherConnectedEntitiesEvent;

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
        Vector3i pos = new Vector3i();
        for (Side side : Side.values()) {
            side.getAdjacentPos(component.getPosition(pos), pos);
            if (provider.isBlockRelevant(pos) && blockEntityRegistry.hasPermanentBlockEntity(pos)) {
                EntityRef ref = blockEntityRegistry.getBlockEntityAt(pos);
                if (ref != null && ref.exists()) {
                    removeNoLongerVisibleComponents(ref);
                }
            }
        }
    }
}
