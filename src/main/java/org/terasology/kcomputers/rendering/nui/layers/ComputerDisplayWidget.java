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
package org.terasology.kcomputers.rendering.nui.layers;

import org.joml.Vector2i;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;
import org.terasology.kallisti.base.util.keyboard.TranslationAWTLWJGL;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.components.KallistiKeyboardComponent;
import org.terasology.kcomputers.events.KallistiKeyPressedEvent;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.events.NUICharEvent;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.util.RectUtility;
import org.terasology.rendering.assets.texture.Texture;

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

    private transient int lastCharacter;

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        EntityRef ref = displayComponent.get().getEntityRef();
        if (ref.hasComponent(KallistiKeyboardComponent.class) && TranslationAWTLWJGL.hasLwjgl(event.getKey().getId())) {
            localPlayer.get().send(new KallistiKeyPressedEvent(
                    new KeyboardInputProvider.Key(
                            event.isDown() ? KeyboardInputProvider.KeyType.PRESSED :
                                    KeyboardInputProvider.KeyType.RELEASED,
                            TranslationAWTLWJGL.toAwt(event.getKey().getId()),
                            event.isDown() ? event.getKey().getId() : lastCharacter
                    )
            ));
            if (event.isDown()) {
                lastCharacter = event.getKey().getId();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCharEvent(NUICharEvent event) {

        EntityRef ref = displayComponent.get().getEntityRef();
        char character = event.getCharacter();
        if (ref.hasComponent(KallistiKeyboardComponent.class) && TranslationAWTLWJGL.hasLwjgl(character)) {
            localPlayer.get().send(new KallistiKeyPressedEvent(
                    new KeyboardInputProvider.Key(
                            event.getKeyboard().isKeyDown(character) ? KeyboardInputProvider.KeyType.PRESSED :
                                    KeyboardInputProvider.KeyType.RELEASED,
                            TranslationAWTLWJGL.toAwt(character),
                            event.getKeyboard().isKeyDown(character) ? character : lastCharacter
                    )
            ));
            if (event.getKeyboard().isKeyDown(character)) {
                lastCharacter = character;
            }
            return true;
        }
        return false;
    }
}
