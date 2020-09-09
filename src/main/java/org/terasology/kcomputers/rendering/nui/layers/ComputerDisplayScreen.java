// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.rendering.nui.layers;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.events.NUIKeyEvent;

/**
 * Default implementation of a computer display screen UI.
 */
public class ComputerDisplayScreen extends CoreScreenLayer {
    @In
    private LocalPlayer localPlayer;

    private ComputerDisplayWidget displayWidget;

    @Override
    public void initialise() {
        displayWidget = find("display", ComputerDisplayWidget.class);
        displayWidget.bindLocalPlayer(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        displayWidget.bindDisplayComponent(new ReadOnlyBinding<KallistiDisplayComponent>() {
            @Override
            public KallistiDisplayComponent get() {
                EntityRef characterEntity = localPlayer.getCharacterEntity();
                CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
                EntityRef target = characterComponent.predictedInteractionTarget;
                return target.getComponent(KallistiDisplayComponent.class);
            }
        });
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (super.onKeyEvent(event)) {
            return true;
        }

        return displayWidget.onKeyEvent(event);
    }
}
