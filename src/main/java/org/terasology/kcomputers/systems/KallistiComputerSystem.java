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
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.util.ListBackedMultiValueMap;
import org.terasology.kallisti.base.util.MultiValueMap;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.PeripheralOCGPU;
import org.terasology.kallisti.simulator.SimulatorComponentContext;
import org.terasology.kallisti.simulator.SimulatorFileSystem;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.TerasologyEntityContext;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.kallisti.ByteArrayStaticByteStorage;
import org.terasology.kcomputers.kallisti.HexFont;
import org.terasology.kcomputers.kallisti.KallistiAsset;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import java.util.*;
import java.util.logging.Logger;

@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiComputerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
	@In
	private WorldProvider provider;
	@In
	private BlockEntityRegistry blockEntityRegistry;

	private Set<KallistiComputerComponent> computers = new HashSet<>();

	@ReceiveEvent
	public void computerActivated(OnActivatedComponent event, EntityRef entity, BlockComponent blockComponent, KallistiComputerComponent computerComponent) {
		init(entity, false);
	}

	@ReceiveEvent
	public void computerDeactivated(BeforeDeactivateComponent event, EntityRef entity, BlockComponent blockComponent, KallistiComputerComponent computerComponent) {
		// TODO: Add Machine.stop()?
		computerComponent.machine = null;
		computers.remove(computerComponent);
	}

	@Override
	public void update(float delta) {
		Iterator<KallistiComputerComponent> computerComponentIterator = computers.iterator();
		while (computerComponentIterator.hasNext()) {
			KallistiComputerComponent computer = computerComponentIterator.next();
			try {
				if (computer.machine == null || !computer.machine.tick(delta)) {
					computer.machine = null;
					computerComponentIterator.remove();
				}
			} catch (Exception e) {
				KComputersUtil.LOGGER.warn("Error updating machine!", e);
				computer.machine = null;
				computerComponentIterator.remove();
			}
		}
	}

	private boolean init(EntityRef ref, boolean force) {
		if (!ref.hasComponent(KallistiComputerComponent.class)) {
			return false;
		}

		KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
		if (computer.machine != null && !force) {
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

							for (Side side : Side.values()) {
								positions.add(new Vector3i(location).add(side.getVector3i()));
							}
						}
					}
				}
			}
		}

		if (kallistiComponents.isEmpty()) {
			return false;
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
			KallistiAsset ocFiles = CoreRegistry.get(AssetManager.class)
					.getAsset(new ResourceUrn("KComputers:opencomputers"), KallistiAsset.class)
					.get();

			computer.machine = new MachineOpenComputers(
					ocFiles.getData().readFully("machine.lua", Charsets.UTF_8),
                    contextComputer,
                    CoreRegistry.get(AssetManager.class)
                            .getAsset(new ResourceUrn("KComputers:unicode-8x16"), HexFont.class)
                            .get().getKallistiFont(),
					1048576, LuaState53.class, false
			);

			KallistiAsset openOsDisk = CoreRegistry.get(AssetManager.class)
					.getAsset(new ResourceUrn("KComputers:disk_openos"), KallistiAsset.class)
					.get();

			byte[] biosEepromCode = ocFiles.getData().readFully("bios.lua");
			int dataSize = 256;

			// The OpenComputers EEPROM keeps 256 bytes of "data" at the end.
			byte[] biosEeprom = new byte[biosEepromCode.length + dataSize];
			System.arraycopy(biosEepromCode, 0, biosEeprom, 0, biosEepromCode.length);

			computer.machine.addComponent(
					new SimulatorComponentContext("test1"),
					new ByteArrayStaticByteStorage(
							biosEeprom
					)
			);

			computer.machine.addComponent(
					new SimulatorComponentContext("test2"),
					openOsDisk
			);

			computer.machine.addComponent(
					new SimulatorComponentContext("test3"),
					new PeripheralOCGPU((MachineOpenComputers) computer.machine, 80, 25, OCGPURenderer.genThirdTierPalette())
			);
		} catch (Exception e) {
			KComputersUtil.LOGGER.warn("Error initializing machine components!", e);
		}

		for (TerasologyEntityContext context : kallistiComponents.keySet()) {
			KComputersUtil.LOGGER.info("adding " + kallistiComponents.get(context).getClass().getName());
			computer.machine.addComponent(context, kallistiComponents.get(context));
		}

		computer.machine.initialize();
		try {
			computer.machine.start();
		} catch (Exception e) {
			KComputersUtil.LOGGER.warn("Error initializing machine!", e);
			computer.machine = null;
			return false;
		}

		computers.add(computer);
		return true;
	}
}
