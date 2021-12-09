// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.rendering.nui.layers;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.kcomputers.components.KallistiComputerComponent;
import org.terasology.kcomputers.events.KallistiChangeComputerStateEvent;
import org.terasology.module.inventory.ui.InventoryGrid;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;

/**
 * Default implementation of a computer container UI, complete
 * with activation and deactivation buttons for a KallistiComputerComponent.
 */
public class ComputerContainerScreen extends CoreScreenLayer {
    @In
    private LocalPlayer localPlayer;

    private InventoryGrid containerInventory;

    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        inventory.setCellOffset(10);

        containerInventory = find("container", InventoryGrid.class);
        containerInventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                EntityRef characterEntity = localPlayer.getCharacterEntity();
                CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
                return characterComponent.predictedInteractionTarget;
            }
        });

        UIButton buttonOn = find("buttonOn", UIButton.class);
        buttonOn.subscribe(widget -> {
            EntityRef characterEntity = localPlayer.getCharacterEntity();
            CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
            EntityRef target = characterComponent.predictedInteractionTarget;
            if (target.hasComponent(KallistiComputerComponent.class)) {
                localPlayer.getClientEntity().send(new KallistiChangeComputerStateEvent(target,
                    localPlayer.getClientEntity(), true));
            }
        });

        UIButton buttonOff = find("buttonOff", UIButton.class);
        buttonOff.subscribe(widget -> {
            EntityRef characterEntity = localPlayer.getCharacterEntity();
            CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
            EntityRef target = characterComponent.predictedInteractionTarget;
            if (target.hasComponent(KallistiComputerComponent.class)) {
                localPlayer.getClientEntity().send(new KallistiChangeComputerStateEvent(target,
                    localPlayer.getClientEntity(), false));
            }
        });
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
