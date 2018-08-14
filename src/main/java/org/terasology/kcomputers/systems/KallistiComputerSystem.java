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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
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
import org.terasology.kcomputers.events.KallistiGatherConnectedEntitiesEvent;
import org.terasology.kcomputers.events.KallistiRegisterComponentRulesEvent;
import org.terasology.kcomputers.events.KallistiChangeComputerStateEvent;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import java.util.*;

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
	public void computerToggle(KallistiChangeComputerStateEvent event, EntityRef ref, BlockComponent blockComponent, KallistiComputerComponent computerComponent) {
		try {
			if (event.getState()) {
				if (computerComponent.getMachine() == null) {
					init(ref, false);
				}
			} else {
				if (computerComponent.getMachine() != null) {
					deinit(ref, computerComponent);
				}
			}
		} catch (Exception e) {
			String s = "Error " + (event.getState() ? "initializing" : "deinitializing") + " computer!";
			EntityRef instigator = event.getCaller();
			if (instigator.exists() && instigator.hasComponent(ClientComponent.class)) {
				instigator.send(new ChatMessageEvent(s + ": " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()), ref));
				if (e.getMessage() == null) {
					KComputersUtil.LOGGER.warn(s, e);
				}
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
	public void computerDeactivated(BeforeDeactivateComponent event, EntityRef ref, BlockComponent blockComponent, KallistiComputerComponent computerComponent) {
		try {
			deinit(ref, computerComponent);
		} catch (Exception e) {
			KComputersUtil.LOGGER.error("Error deinitializing computer!", e);
		}
	}

	@ReceiveEvent
	public void addConnectedEntitiesConnectable(KallistiGatherConnectedEntitiesEvent event, EntityRef ref, BlockComponent blockComponent, KallistiConnectableComponent connectableComponent) {
		Vector3i pos = blockComponent.getPosition();

		for (Side side : Side.values()) {
			Vector3i location = new Vector3i(pos).add(side.getVector3i());
			if (provider.isBlockRelevant(location) && blockEntityRegistry.hasPermanentBlockEntity(location)) {
				event.addEntity(blockEntityRegistry.getBlockEntityAt(location));
			}
		}
	}

	@ReceiveEvent
	public void addConnectedEntitiesInventory(KallistiGatherConnectedEntitiesEvent event, EntityRef entity, KallistiInventoryWithContainerComponent component) {
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
		contextsPerPos.add(ref.getComponent(BlockComponent.class).getPosition(), contextComputer);

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
				Vector3i vecSided = new Vector3i(vec).add(side.getVector3i());
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
