// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.systems;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaStackTraceElement;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.base.util.CollectionBackedMultiValueMap;
import org.terasology.kallisti.base.util.MultiValueMap;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.TerasologyEntityContext;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.components.KallistiConnectableComponent;
import org.terasology.kcomputers.components.KallistiInventoryWithContainerComponent;
import org.terasology.kcomputers.components.KallistiMachineProvider;
import org.terasology.kcomputers.components.parts.KallistiMemoryComponent;
import org.terasology.kcomputers.events.KallistiChangeComputerStateEvent;
import org.terasology.kcomputers.events.KallistiGatherConnectedEntitiesEvent;
import org.terasology.kcomputers.events.KallistiRegisterComponentRulesEvent;
import org.terasology.module.inventory.components.InventoryComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This system handles computer initialization, as well as - currently - entity
 * propagation for KallistiGatherConnectedEntitiesEvent.
 *
 * @see KallistiGatherConnectedEntitiesEvent
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiComputerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider provider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private Set<EntityRef> computers = new HashSet<>();

    @ReceiveEvent
    public void computerToggle(KallistiChangeComputerStateEvent event, EntityRef target) {
        // TODO: Disallow faraway toggling of computers
        if (!event.getMachine().hasComponent(KallistiComputerComponent.class)) {
            return;
        }

        KallistiComputerComponent computerComponent = event.getMachine().getComponent(KallistiComputerComponent.class);

        try {
            if (event.getState()) {
                if (computerComponent.getMachine() == null) {
                    init(event.getMachine(), false);
                }
            } else {
                if (computerComponent.getMachine() != null) {
                    deinit(event.getMachine(), computerComponent);
                }
            }
        } catch (Exception e) {
            String s = "Error " + (event.getState() ? "initializing" : "deinitializing") + " computer!";
            EntityRef instigator = event.getCaller();
            if (instigator.exists() && instigator.hasComponent(ClientComponent.class)) {
                instigator.send(new ChatMessageEvent(
                        s + ": " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()),
                        event.getMachine()));
                KComputersUtil.LOGGER.warn(s, e);
            } else {
                KComputersUtil.LOGGER.error(s, e);
            }
        }
    }

    private void deinit(EntityRef ref, KallistiComputerComponent computerComponent) throws Exception {
        Exception exception = null;

        if (computerComponent.getMachine() != null && computerComponent.getMachine().getState() == Machine.MachineState.RUNNING) {
            try {
                computerComponent.getMachine().stop();
            } catch (Exception e) {
                exception = e;
            }
        }

        computerComponent.setMachine(null);
        computers.remove(ref);

        if (exception != null) {
            throw exception;
        }
    }

    @ReceiveEvent
    public void computerDeactivated(BeforeDeactivateComponent event,
                                    EntityRef ref,
                                    BlockComponent blockComponent,
                                    KallistiComputerComponent computerComponent) {
        try {
            deinit(ref, computerComponent);
        } catch (Exception e) {
            KComputersUtil.LOGGER.error("Error deinitializing computer!", e);
        }
    }

    @ReceiveEvent
    public void addConnectedEntitiesConnectable(KallistiGatherConnectedEntitiesEvent event,
                                                EntityRef ref,
                                                BlockComponent blockComponent,
                                                KallistiConnectableComponent connectableComponent) {
        Vector3ic pos = blockComponent.getPosition(new Vector3i());
        Vector3i location = new Vector3i();
        for (Side side : Side.values()) {
            pos.add(side.direction(), location);
            if (provider.isBlockRelevant(location) && blockEntityRegistry.hasPermanentBlockEntity(location)) {
                event.addEntity(blockEntityRegistry.getBlockEntityAt(location));
            }
        }
    }

    @ReceiveEvent
    public void addConnectedEntitiesInventory(KallistiGatherConnectedEntitiesEvent event,
                                              EntityRef entity,
                                              KallistiInventoryWithContainerComponent component) {
        if (entity.hasComponent(InventoryComponent.class)) {
            InventoryComponent inv = entity.getComponent(InventoryComponent.class);

            for (EntityRef ref : inv.itemSlots) {
                event.addEntity(ref);
            }
        }
    }

    @Override
    public void update(float delta) {
        Iterator<EntityRef> computerRefIterator = computers.iterator();
        while (computerRefIterator.hasNext()) {
            EntityRef ref = computerRefIterator.next();
            if (!ref.exists() || !ref.hasComponent(KallistiComputerComponent.class)) {
                computerRefIterator.remove();
                continue;
            }
            KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);

            try {
                if (computer.getMachine() == null || !computer.getMachine().tick(delta)) {
                    computer.setMachine(null);
                    computer.onMachineChanged(ref);
                    computerRefIterator.remove();
                }
            } catch (Exception e) {
                KComputersUtil.LOGGER.warn("Error updating machine!", e);
                if (e instanceof LuaRuntimeException) {
                    for (LuaStackTraceElement element : ((LuaRuntimeException) e).getLuaStackTrace()) {
                        KComputersUtil.LOGGER.warn("LUA: " + element.toString());
                    }
                }
                computer.setMachine(null);
                computer.onMachineChanged(ref);
                computerRefIterator.remove();
            }
        }
    }

    private void init(EntityRef ref, boolean force) throws Exception {
        if (!ref.hasComponent(KallistiComputerComponent.class)) {
            throw new Exception("No computer in entity!");
        }

        KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
        if (computer.getMachine() != null && !force) {
            return;
        }

        KallistiGatherConnectedEntitiesEvent gatherEvent = new KallistiGatherConnectedEntitiesEvent();
        gatherEvent.addEntity(ref);

        Map<ComponentContext, Object> kallistiComponents = new HashMap<>();
        MultiValueMap<Vector3i, TerasologyEntityContext> contextsPerPos = new CollectionBackedMultiValueMap<>(new HashMap<>(), ArrayList::new);

        TerasologyEntityContext contextComputer = new TerasologyEntityContext(ref.getId(), -1);
        contextsPerPos.add(ref.getComponent(BlockComponent.class).getPosition(new Vector3i()), contextComputer);

        for (EntityRef lref : gatherEvent.getEntities()) {
            kallistiComponents.putAll(KComputersUtil.gatherKallistiComponents(lref));
        }

        for (Vector3i vec : contextsPerPos.keys()) {
            // add self
            for (TerasologyEntityContext to : contextsPerPos.values(vec)) {
                for (TerasologyEntityContext from : contextsPerPos.values(vec)) {
                    if (from != to) {
                        from.addNeighbor(to);
                    }
                }
            }
            // add neighbors
            for (Side side : Side.values()) {
                Vector3i vecSided = new Vector3i(vec).add(side.direction());
                for (TerasologyEntityContext to : contextsPerPos.values(vecSided)) {
                    for (TerasologyEntityContext from : contextsPerPos.values(vec)) {
                        // It will add both ways, as we will be iterating through the parameters
                        // swapped in time.
                        from.addNeighbor(to);
                    }
                }
            }
        }

        int memorySize = 0;
        KallistiMachineProvider provider = null;

        Iterator<Map.Entry<ComponentContext, Object>> it = kallistiComponents.entrySet().iterator();
        while (it.hasNext()) {
            Object o = it.next().getValue();
            if (o instanceof KallistiMemoryComponent) {
                memorySize += ((KallistiMemoryComponent) o).amount;
            } else if (o instanceof KallistiMachineProvider) {
                if (provider != null && provider != o) {
                    throw new Exception("Found more than one CPU!");
                } else {
                    provider = (KallistiMachineProvider) o;
                }
            } else {
                continue;
            }

            it.remove();
        }

        if (provider == null) {
            throw new Exception("No CPU found!");
        } else if (memorySize < 128) {
            throw new Exception("Not enough memory!");
        }

        Machine machine = provider.create(
                contextComputer,
                ref,
                ref, /* TODO: Actually provide the correct ref */
                memorySize
        );

        ref.send(new KallistiRegisterComponentRulesEvent(machine));
        computer.setMachine(machine);

        for (Map.Entry<ComponentContext, Object> entry : kallistiComponents.entrySet()) {
            KComputersUtil.LOGGER.info("adding " + entry.getValue().getClass().getName());
            computer.getMachine().addComponent(entry.getKey(), entry.getValue());
        }

        computer.getMachine().initialize();
        try {
            computer.getMachine().start();
        } catch (Exception e) {
            computer.setMachine(null);
            throw new Exception(e);
        }

        computers.add(ref);
        computer.onMachineChanged(ref);
    }
}
