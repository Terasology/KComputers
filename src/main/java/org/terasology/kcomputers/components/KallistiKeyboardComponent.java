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
package org.terasology.kcomputers.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Component providing a Kallisti-compatible keyboard.
 */
public class KallistiKeyboardComponent implements Component, KeyboardInputProvider {
	private transient ArrayList<Key> keyQueue = new ArrayList<>();

	@Override
	public boolean hasNextKey() {
		return !keyQueue.isEmpty();
	}

	@Override
	public Key nextKey() {
		return keyQueue.remove(0);
	}

	/**
	 * Add a key to the queue.
	 * @param key The key to add.
	 */
	public void addKey(Key key) {
		if (key.getCode() != 0) {
			keyQueue.add(key);
		}
	}
}
