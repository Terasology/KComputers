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
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.TerasologyPositionalContext;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.*;

public class KallistiComputerSystem {
	private boolean init(WorldProvider provider, BlockEntityRegistry blockEntityRegistry, EntityRef ref) {
		KallistiComputerComponent computer = ref.getComponent(KallistiComputerComponent.class);
		Vector3i pos = new Vector3i(ref.getComponent(LocationComponent.class).getWorldPosition());

		Map<TerasologyPositionalContext, Object> kallistiComponents = new HashMap<>();
		Set<Vector3i> visitedPositions = new HashSet<>();
		LinkedList<Vector3i> positions = new LinkedList<>();
		positions.add(pos);

		while (!positions.isEmpty()) {
			Vector3i location = positions.remove();
			if (visitedPositions.add(location)) {
				if (provider.isBlockRelevant(location)) {
					EntityRef lref = blockEntityRegistry.getBlockEntityAt(location);
					if (lref != null) {
						Collection<Object> kc = KComputersUtil.getKallistiComponents(lref);
						if (!kc.isEmpty()) {
							int id = 0;
							for (Object o : kc) {
								kallistiComponents.put(new TerasologyPositionalContext(location, id++), o);
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

		computer.machine = new MachineOpenComputers(
				"", new TerasologyPositionalContext(pos, 0), null, 1048576, LuaState53.class, false
		);

		for (TerasologyPositionalContext context : kallistiComponents.keySet()) {
			computer.machine.addComponent(context, kallistiComponents.get(context));
		}

		computer.machine.initialize();
		return true;
	}
}
