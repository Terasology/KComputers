// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.rendering.nui.layers;

import org.joml.Vector2i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;
import org.terasology.kallisti.base.util.keyboard.TranslationAWTLWJGL;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.components.KallistiKeyboardComponent;
import org.terasology.kcomputers.events.KallistiKeyPressedEvent;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.util.RectUtility;

/**
 * Widget which allows rendering the buffer of a KallistiDisplayComponent as an in-game UI, as well as proxying key
 * presses to a KallistiKeyboardComponent.
 * <p>
 * TODO: Currently, the keyboard component must be on the same block as the KallistiDisplayComponent. This should
 * probably not have to be the case, but it is not a high priority as the current behaviour is sufficiently intuitive
 * for end users.
 */
public class ComputerDisplayWidget extends CoreWidget {
    private transient Binding<KallistiDisplayComponent> displayComponent;
    private transient Binding<EntityRef> localPlayer;
    private transient int lastCharacter;

    public void bindLocalPlayer(Binding<EntityRef> binding) {
        this.localPlayer = binding;
    }

    public void bindDisplayComponent(Binding<KallistiDisplayComponent> display) {
        this.displayComponent = display;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Texture texture = displayComponent.get().getTexture();
        canvas.drawTexture(texture, RectUtility.createFromMinAndSize(0, 0, displayComponent.get().getPixelWidth(),
                displayComponent.get().getPixelHeight()));
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(displayComponent.get().getPixelWidth(), displayComponent.get().getPixelHeight());
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        EntityRef ref = displayComponent.get().getEntityRef();
        if (ref.hasComponent(KallistiKeyboardComponent.class) && TranslationAWTLWJGL.hasLwjgl(event.getKey().getId())) {
//			KComputersUtil.LOGGER.warn("Known key " + event.getKey().getId());
            localPlayer.get().send(new KallistiKeyPressedEvent(
                    new KeyboardInputProvider.Key(
                            event.isDown() ? KeyboardInputProvider.KeyType.PRESSED :
                                    KeyboardInputProvider.KeyType.RELEASED,
                            TranslationAWTLWJGL.toAwt(event.getKey().getId()),
                            event.isDown() ? event.getKeyCharacter() : lastCharacter
                    )
            ));
            if (event.isDown()) {
                lastCharacter = event.getKeyCharacter();
            }
            return true;
//		} else {
//			KComputersUtil.LOGGER.warn("Unknown key " + event.getKey().getId());
        }

        return false;
    }

}
