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
package org.terasology.kcomputers.events;

import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

@ServerEvent
public class KallistiKeyPressedEvent extends NetworkEvent {
	private int state;
	private int code;
	private int chr;

	public KallistiKeyPressedEvent() {
		state = 0;
		code = 0;
		chr = 0;
	}

	public KallistiKeyPressedEvent(KeyboardInputProvider.Key key) {
		state = key.getType().ordinal();
		code = key.getCode();
		chr = key.getChar();
	}

	public KeyboardInputProvider.Key getKey() {
		return new KeyboardInputProvider.Key(
				KeyboardInputProvider.KeyType.values()[state],
				code,
				chr
		);
	}
}
