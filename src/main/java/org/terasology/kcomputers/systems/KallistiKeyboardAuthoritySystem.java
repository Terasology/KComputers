// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.kcomputers.components.KallistiKeyboardComponent;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiKeyPressedEvent;

/**
 * This system handles forwarding Kallisti keyboard keypresses from the event to the component.
 *
 * @see KallistiKeyboardComponent
 * @see KallistiKeyPressedEvent
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiKeyboardAuthoritySystem extends BaseComponentSystem {
    @ReceiveEvent
    public void onAttachComponents(KallistiAttachComponentsEvent event, EntityRef ref,
                                   KallistiKeyboardComponent component) {
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
