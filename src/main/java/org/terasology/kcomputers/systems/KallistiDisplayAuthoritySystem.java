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
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.events.KallistiRequestInitialEvent;
import org.terasology.kcomputers.events.KallistiSyncInitialEvent;
import org.terasology.network.ClientComponent;
import org.terasology.world.block.BlockComponent;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiDisplayAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
	private Set<EntityRef> displays = new HashSet<>();

	@ReceiveEvent
	public void displayActivated(OnActivatedComponent event, EntityRef entity, KallistiDisplayComponent component) {
		displays.add(entity);
	}

	@ReceiveEvent
	public void displayDeactivated(BeforeDeactivateComponent event, EntityRef entity, KallistiDisplayComponent component) {
		displays.remove(entity);
	}

	@Override
	public void update(float delta) {
		Iterator<EntityRef> displayIt = displays.iterator();
		while (displayIt.hasNext()) {
			EntityRef ref = displayIt.next();
			/* if (!ref.exists()) {
				displayIt.remove();
				continue;
			} */

			KallistiDisplayComponent component = ref.getComponent(KallistiDisplayComponent.class);
			if (component.getSource() != null) {
				KComputersUtil.synchronize(ref, ref, component.getSource(), Synchronizable.Type.INITIAL);
			}
		}
	}

	@ReceiveEvent(components = ClientComponent.class)
	public void onRequestInitialUpdate(KallistiRequestInitialEvent event, EntityRef entity) {
		for (Object o : event.getMachine().iterateComponents()) {
			if (o instanceof KallistiDisplayComponent && ((KallistiDisplayComponent) o).getSource() != null) {
				KComputersUtil.synchronize(event.getInstigator(), event.getMachine(), ((KallistiDisplayComponent) o).getSource(), Synchronizable.Type.INITIAL);
			}
		}
	}
}
