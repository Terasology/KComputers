// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.assets;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

@RegisterAssetFileFormat
public class KallistiArchiveFormat extends AbstractAssetFileFormat<KallistiArchiveData> {
    public KallistiArchiveFormat() {
        super("zip");
    }

    @Override
    public KallistiArchiveData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (InputStream stream = inputs.get(0).openStream()) {
            return new KallistiArchiveData(new ZipInputStream(stream));
        } catch (IOException e) {
            throw new IOException("Failed to load Kallisti archive; " + e.getMessage(), e);
        }
    }
}
