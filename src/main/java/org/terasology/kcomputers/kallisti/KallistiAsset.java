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
package org.terasology.kcomputers.kallisti;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.annotations.RegisterAssetType;
import org.terasology.kallisti.oc.OCFont;
import org.terasology.module.sandbox.API;

import java.nio.charset.Charset;

@RegisterAssetType(factoryClass = KallistiAsset.Factory.class, folderName = "kallisti")
public class KallistiAsset extends Asset<KallistiAssetData> {
    public static class Factory implements AssetFactory<KallistiAsset, KallistiAssetData> {
        @Override
        public KallistiAsset build(ResourceUrn urn, AssetType<KallistiAsset, KallistiAssetData> assetType, KallistiAssetData data) {
            return new KallistiAsset(urn, assetType, data);
        }
    }

    private KallistiAssetData data;

    public KallistiAsset(ResourceUrn urn, AssetType<?, KallistiAssetData> assetType, KallistiAssetData data) {
        super(urn, assetType);
        reload(data);
    }

    public KallistiAssetData getData() {
        return data;
    }

    @Override
    protected void doReload(KallistiAssetData data) {
        this.data = data;
    }
}
