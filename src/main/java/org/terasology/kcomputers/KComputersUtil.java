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
package org.terasology.kcomputers;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kcomputers.components.KallistiComponentContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class KComputersUtil {
	private KComputersUtil() {

	}

	public static Collection<Object> getKallistiComponents(EntityRef ref) {
		int collectionCount = 0;
		Collection<Object> kc = Collections.emptySet();

		for (Component component : ref.iterateComponents()) {
			if (component instanceof KallistiComponentContainer) {
				Collection<Object> kcc = ((KallistiComponentContainer) component).getKallistiComponents();

				if (collectionCount == 0) {
					kc = kcc;
				} else if (collectionCount == 1) {
					Collection<Object> oldKcc = kc;
					kc = new HashSet<>();
					kc.addAll(oldKcc);
					kc.addAll(kcc);
				} else {
					kc.addAll(kcc);
				}

				collectionCount++;
			}
		}

		return kc;
	}
}
