// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.systems;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.kcomputers.components.KallistiDisplayCandidateComponent;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.components.MeshRenderComponent;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiRegisterSyncListenerEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class KallistiDisplayClientSystem extends BaseComponentSystem {
    @In
    private LocalPlayer player;
    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onAttachComponents(KallistiAttachComponentsEvent event, EntityRef ref, KallistiDisplayCandidateComponent component) {
        event.addComponent(ref, component.getDisplay());
    }

    @ReceiveEvent
    public void onAttachComponents(KallistiAttachComponentsEvent event, EntityRef ref, KallistiDisplayComponent component) {
        event.addComponent(ref, component);
    }

    @ReceiveEvent
    public void displayActivated(OnActivatedComponent event,
                                 EntityRef entity,
                                 BlockComponent blockComponent,
                                 KallistiDisplayCandidateComponent component,
                                 MeshRenderComponent meshRenderComponent) {
        if (!component.multiBlock) {
            KallistiDisplayComponent displayComponent = new KallistiDisplayComponent();
            displayComponent.configure(
                entityManager, entity, component, meshRenderComponent
            );
            entity.addComponent(displayComponent);
            player.getClientEntity().send(new KallistiRegisterSyncListenerEvent(player.getClientEntity(), entity));
        }
    }

    @ReceiveEvent
    public void displayDeactivated(BeforeDeactivateComponent event,
                                   EntityRef entity,
                                   KallistiDisplayCandidateComponent component,
                                   MeshRenderComponent meshRenderComponent) {
        meshRenderComponent.clear();
    }
}
