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
package org.terasology.kcomputers.assets;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.annotations.RegisterAssetType;

/**
 * Asset type used for making read-only ZIP files available as filesystem
 * structures to Kallisti virtual machines.
 */
@RegisterAssetType(factoryClass = KallistiArchive.Factory.class, folderName = "kallisti")
public class KallistiArchive extends Asset<KallistiArchiveData> {
    public static class Factory implements AssetFactory<KallistiArchive, KallistiArchiveData> {
        @Override
        public KallistiArchive build(ResourceUrn urn, AssetType<KallistiArchive, KallistiArchiveData> assetType,
                                     KallistiArchiveData data) {
            return new KallistiArchive(urn, assetType, data);
        }
    }

    private KallistiArchiveData data;

    public KallistiArchive(ResourceUrn urn, AssetType<?, KallistiArchiveData> assetType, KallistiArchiveData data) {
        super(urn, assetType);
        reload(data);
    }

    public KallistiArchiveData getData() {
        return data;
    }

    @Override
    protected void doReload(KallistiArchiveData data) {
        this.data = data;
    }
}
