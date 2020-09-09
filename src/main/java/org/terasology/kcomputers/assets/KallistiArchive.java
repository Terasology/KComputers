// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.assets;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetFactory;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetType;

/**
 * Asset type used for making read-only ZIP files available as filesystem structures to Kallisti virtual machines.
 */
@RegisterAssetType(factoryClass = KallistiArchive.Factory.class, folderName = "kallisti")
public class KallistiArchive extends Asset<KallistiArchiveData> {
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

    public static class Factory implements AssetFactory<KallistiArchive, KallistiArchiveData> {
        @Override
        public KallistiArchive build(ResourceUrn urn, AssetType<KallistiArchive, KallistiArchiveData> assetType,
                                     KallistiArchiveData data) {
            return new KallistiArchive(urn, assetType, data);
        }
    }
}
