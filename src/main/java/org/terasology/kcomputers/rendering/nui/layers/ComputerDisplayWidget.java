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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.Keyboard;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;
import org.terasology.kallisti.base.util.keyboard.TranslationAWTLWJGL;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.components.KallistiDisplayComponent;
import org.terasology.kcomputers.components.KallistiKeyboardComponent;
import org.terasology.kcomputers.events.KallistiKeyPressedEvent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.events.NUIKeyEvent;

public class ComputerDisplayWidget extends CoreWidget {
	private transient Binding<KallistiDisplayComponent> displayComponent;
	private transient Binding<EntityRef> localPlayer;

	public void bindLocalPlayer(Binding<EntityRef> binding) {
		this.localPlayer = binding;
	}

	public void bindDisplayComponent(Binding<KallistiDisplayComponent> displayComponent) {
		this.displayComponent = displayComponent;
	}

	@Override
	public void onDraw(Canvas canvas) {
		Texture texture = displayComponent.get().getTexture();
		canvas.drawTexture(texture, Rect2i.createFromMinAndSize(0, 0, displayComponent.get().getPixelWidth(), displayComponent.get().getPixelHeight()));
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
//			KComputersUtil.LOGGER.warn("Known key " + event.getKey().getId());
			localPlayer.get().send(new KallistiKeyPressedEvent(
					new KeyboardInputProvider.Key(
							event.isDown() ? KeyboardInputProvider.KeyType.PRESSED : KeyboardInputProvider.KeyType.RELEASED,
							TranslationAWTLWJGL.toAwt(event.getKey().getId()),
							event.isDown() ? event.getKeyCharacter() : lastCharacter
					)
			));
			if (event.isDown()) {
				lastCharacter = event.getKeyCharacter();
			}
			return true;
		} else {
//			KComputersUtil.LOGGER.warn("Unknown key " + event.getKey().getId());
		}

		return false;
	}
}
