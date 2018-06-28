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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.components.MeshRenderComponent;
import org.terasology.kcomputers.events.KallistiRegisterSyncListenerEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;

@RegisterSystem(RegisterMode.CLIENT)
public class KallistiDisplayClientSystem extends BaseComponentSystem {
	@In
	private LocalPlayer player;
	@In
	private EntityManager entityManager;

	@ReceiveEvent
	public void displayActivated(OnActivatedComponent event, EntityRef entity, BlockComponent blockComponent, KallistiDisplayComponent component, MeshRenderComponent meshRenderComponent) {
		component.setMeshRenderComponent(entityManager, entity, meshRenderComponent);
		player.getClientEntity().send(new KallistiRegisterSyncListenerEvent(player.getClientEntity(), entity));
	}

	@ReceiveEvent
	public void displayDeactivated(BeforeDeactivateComponent event, EntityRef entity, KallistiDisplayComponent component, MeshRenderComponent meshRenderComponent) {
		meshRenderComponent.clear();
	}
}
