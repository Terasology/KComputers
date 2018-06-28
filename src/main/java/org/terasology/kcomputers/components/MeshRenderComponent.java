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
package org.terasology.kcomputers.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.logic.MeshComponent;

import java.util.HashMap;
import java.util.Map;

public class MeshRenderComponent implements Component {
	private final Map<String, EntityRef> meshes = new HashMap<>();

	private boolean remove(EntityRef ref) {
		if (ref != null) {
			if (ref.getComponent(MeshComponent.class) != null) {
				ref.getComponent(MeshComponent.class).mesh.dispose();
				ref.getComponent(MeshComponent.class).material.dispose();
			}
			ref.destroy();
			return true;
		} else {
			return false;
		}
	}

	public boolean remove(String name) {
		return remove(meshes.remove(name));
	}

	public MeshComponent get(String name) {
		EntityRef ref = meshes.get(name);
		return ref != null ? ref.getComponent(MeshComponent.class) : null;
	}

	public boolean add(EntityManager manager, String name, Vector3f location, MeshComponent component) {
		EntityRef ref = meshes.get(name);
		if (ref == null) {
			EntityBuilder builder = manager.newBuilder();
			builder.setPersistent(false);
			builder.addComponent(new LocationComponent(location));
			builder.addComponent(component);
			ref = builder.build();
			meshes.put(name, ref);
			return true;
		} else {
			return false;
		}
	}

	public void clear() {
		for (EntityRef ref : meshes.values()) {
			remove(ref);
		}
		meshes.clear();
	}
}
