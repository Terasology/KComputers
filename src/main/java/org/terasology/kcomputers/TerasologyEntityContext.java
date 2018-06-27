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
package org.terasology.kcomputers;

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.interfaces.ConnectedContext;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;

import java.util.ArrayList;
import java.util.List;

public class TerasologyEntityContext implements ComponentContext, ConnectedContext {
	private final long entityId;
	private final int id;
	private final List<ComponentContext> neighbors = new ArrayList<>();

	public TerasologyEntityContext(long entityId, int id) {
		this.entityId = entityId;
		this.id = id;
	}

	public void addNeighbor(TerasologyEntityContext context) {
		neighbors.add(context);
	}

	@Override
	public String identifier() {
		return entityId + ":" + id;
	}

	@Override
	public List<ComponentContext> getNeighbors() {
		return neighbors;
	}
}
