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
package org.terasology.kcomputers.components.parts;

import com.google.common.collect.ImmutableSet;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kcomputers.components.KallistiComponentContainer;
import org.terasology.kcomputers.kallisti.ByteArrayStaticByteStorage;
import org.terasology.kcomputers.kallisti.KallistiArchive;
import org.terasology.registry.CoreRegistry;

import java.util.Collection;

public class KallistiEEPROMAssetedComponent implements Component, KallistiComponentContainer {
    public String assetName;
    public String filename;

    @Override
    public Collection<Object> getKallistiComponents(EntityRef entity) {
        KallistiArchive archive = CoreRegistry.get(AssetManager.class)
                .getAsset(new ResourceUrn(assetName), KallistiArchive.class)
                .get();

        byte[] biosEepromCode = archive.getData().readFully(filename);
        int dataSize = 256;

        // The OpenComputers EEPROM keeps 256 bytes of "data" at the end.
        byte[] biosEeprom = new byte[biosEepromCode.length + dataSize];
        System.arraycopy(biosEepromCode, 0, biosEeprom, 0, biosEepromCode.length);

        return ImmutableSet.of(new ByteArrayStaticByteStorage(biosEeprom));
    }
}
