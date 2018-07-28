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

import org.slf4j.Logger;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kcomputers.components.KallistiComponentContainer;
import org.terasology.kcomputers.events.KallistiSyncDeltaEvent;
import org.terasology.kcomputers.events.KallistiSyncInitialEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class KComputersUtil {
	public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("KComputers");
	private KComputersUtil() {

	}

	public static byte[] toByteArray(InputStream stream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = stream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, len);
		}
		byte[] out = outputStream.toByteArray();
		outputStream.close();
		return out;
	}

	public static void synchronize(EntityRef target, Synchronizable syncer, Synchronizable.Type type, Collection<EntityRef> targets) {
		try {
			if (!syncer.hasSyncPacket(type)) {
				return;
			}

			switch (type) {
				case INITIAL:
					KallistiSyncInitialEvent syncInitial = new KallistiSyncInitialEvent(target, syncer);
					targets.forEach((t) -> {
						if (t.exists()) t.send(syncInitial);
					});
					break;
				case DELTA:
					KallistiSyncDeltaEvent syncDelta = new KallistiSyncDeltaEvent(target, syncer);
					targets.forEach((t) -> {
						if (t.exists()) t.send(syncDelta);
					});
					break;
			}
		} catch (IOException e) {
			KComputersUtil.LOGGER.warn("Error syncing to client!", e);
		}
	}

	public static Collection<Object> getKallistiComponents(EntityRef ref) {
		int collectionCount = 0;
		Collection<Object> kc = Collections.emptySet();

		for (Component component : ref.iterateComponents()) {
			if (component instanceof KallistiComponentContainer) {
				Collection<Object> kcc = ((KallistiComponentContainer) component).getKallistiComponents(ref);

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
