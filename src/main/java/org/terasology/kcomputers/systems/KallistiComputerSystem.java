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

import com.google.common.base.Charsets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaStackTraceElement;
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.base.util.ListBackedMultiValueMap;
import org.terasology.kallisti.base.util.MultiValueMap;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.PeripheralOCGPU;
import org.terasology.kallisti.simulator.SimulatorComponentContext;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.TerasologyEntityContext;
import org.terasology.kcomputers.components.KallistiComponentContainer;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.components.KallistiConnectableComponent;
import org.terasology.kcomputers.components.KallistiMachineProvider;
import org.terasology.kcomputers.components.parts.KallistiMemoryComponent;
import org.terasology.kcomputers.components.parts.KallistiOpenComputersGPUComponent;
import org.terasology.kcomputers.events.KallistiToggleComputerEvent;
import org.terasology.kcomputers.kallisti.ByteArrayStaticByteStorage;
import org.terasology.kcomputers.kallisti.HexFont;
import org.terasology.kcomputers.kallisti.KallistiArchive;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import java.util.*;

@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiComputerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
	@In
	private WorldProvider provider;
	@In
	private BlockEntityRegistry blockEntityRegistry;

	private Set<EntityRef> computers = new HashSet<>();

	@ReceiveEvent
	public void computerToggle(KallistiToggleComputerEvent event, EntityRef ref, BlockComponent blockComponent, KallistiComputerComponent computerComponent) {
		if (event.getState()) {
			if (computerComponent.getMachine() == null) {
				init(ref, false);
			}
		} else {
			if (computerComponent.getMachine() != null) {
				deinit(ref, computerComponent);
			}
		}
	}

	private void deinit(EntityRef ref, KallistiComputerComponent computerComponent) {
		if (computerComponent.getMachine() != null && computerComponent.getMachine().getState() == Machine.MachineState.RUNNING) {
			try {
				computerComponent.getMachine().stop();
			} catch (Exception e) {
				KComputersUtil.LOGGER.warn("Error stopping machine!", e);
			}
		}
		computerComponent.setMachine(null);
		computers.remove(ref);
	}

	@ReceiveEvent
	public void computerDeactivated(BeforeDeactivateComponent event, EntityRef ref, BlockComponent blockComponent, KallistiComputerComponent computerComponent) {
		deinit(ref, computerComponent);
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

	private boolean init(EntityRef ref, boolean force) {
		if (!ref.hasComponent(KallistiComputerComponent.class)) {
			return false;
		}

		KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
		if (computer.getMachine() != null && !force) {
			return true;
		}

		Vector3i pos = ref.getComponent(BlockComponent.class).getPosition();

		Map<TerasologyEntityContext, Object> kallistiComponents = new HashMap<>();
		MultiValueMap<Vector3i, TerasologyEntityContext> contextsPerPos = new ListBackedMultiValueMap<>(new HashMap<>(), ArrayList::new);
		Set<Vector3i> visitedPositions = new HashSet<>();
		LinkedList<Vector3i> positions = new LinkedList<>();

		positions.add(pos);
		TerasologyEntityContext contextComputer = new TerasologyEntityContext(ref.getId(), -1);
		contextsPerPos.add(pos, contextComputer);

		while (!positions.isEmpty()) {
			Vector3i location = positions.remove();
			if (visitedPositions.add(location)) {
				if (provider.isBlockRelevant(location) && blockEntityRegistry.hasPermanentBlockEntity(location)) {
					EntityRef lref = location.equals(pos) ? ref : blockEntityRegistry.getBlockEntityAt(location);
					if (lref != null) {
						Collection<Object> kc = KComputersUtil.getKallistiComponents(lref);
						if (!kc.isEmpty()) {
							int id = 0;
							for (Object o : kc) {
								TerasologyEntityContext context = new TerasologyEntityContext(lref.getId(), id++);

								kallistiComponents.put(context, o);
							}
						}

						if (lref.hasComponent(KallistiConnectableComponent.class)) {
							for (Side side : Side.values()) {
								positions.add(new Vector3i(location).add(side.getVector3i()));
							}
						}
					}
				}
			}
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

		try {
			int memorySize = 0;
			KallistiMachineProvider provider = null;

			Iterator<Map.Entry<TerasologyEntityContext, Object>> it = kallistiComponents.entrySet().iterator();
			while (it.hasNext()) {
				Object o = it.next().getValue();
				if (o instanceof KallistiMemoryComponent) {
					memorySize += ((KallistiMemoryComponent) o).getAmount();
				} else if (o instanceof KallistiMachineProvider) {
					if (provider != null && provider != o) {
						KComputersUtil.LOGGER.error("Provided more than one machine provider!");
						return false;
					} else {
						provider = (KallistiMachineProvider) o;
					}
				} else {
					continue;
				}

				it.remove();
			}

			if (provider == null) {
				KComputersUtil.LOGGER.error("Provided no machine provider!");
				return false;
			} else if (memorySize < 256) {
				KComputersUtil.LOGGER.error("Not enough memory!");
				return false;
			}

			Machine machine = provider.create(
					contextComputer,
					ref,
					ref, /* TODO: Actually provide the correct ref */
					memorySize
			);

			computer.setMachine(machine);
		} catch (Exception e) {
			KComputersUtil.LOGGER.warn("Error initializing machine components!", e);
		}

		for (Map.Entry<TerasologyEntityContext, Object> entry : kallistiComponents.entrySet()) {
			KComputersUtil.LOGGER.info("adding " + entry.getValue().getClass().getName());
			computer.getMachine().addComponent(entry.getKey(), entry.getValue());
		}

		computer.getMachine().initialize();
		try {
			computer.getMachine().start();
		} catch (Exception e) {
			KComputersUtil.LOGGER.warn("Error initializing machine!", e);
			computer.setMachine(null);
			return false;
		}

		computers.add(ref);
		computer.onMachineChanged(ref);
		return true;
	}
}
