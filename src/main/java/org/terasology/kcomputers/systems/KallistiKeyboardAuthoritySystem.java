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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.kcomputers.components.KallistiKeyboardComponent;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiKeyPressedEvent;
import org.terasology.logic.characters.CharacterComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiKeyboardAuthoritySystem extends BaseComponentSystem {
	@ReceiveEvent
	public void onAttachComponents(KallistiAttachComponentsEvent event, EntityRef ref, KallistiKeyboardComponent component) {
		event.addComponent(ref, component);
	}

	@ReceiveEvent
	public void onKeyPressed(KallistiKeyPressedEvent event, EntityRef player) {
		CharacterComponent characterComponent = player.getComponent(CharacterComponent.class);
		EntityRef target = characterComponent.authorizedInteractionTarget;
		if (target != null && target.hasComponent(KallistiKeyboardComponent.class)) {
			target.getComponent(KallistiKeyboardComponent.class).addKey(event.getKey());
		}
	}
}
