// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.assets;

import com.google.common.base.Charsets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.kallisti.oc.OCFont;
import org.terasology.kcomputers.KComputersUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RegisterAssetFileFormat
public class HexFontFormat extends AbstractAssetFileFormat<HexFontData> {
    public HexFontFormat() {
        super("hex");
    }

    @Override
    public HexFontData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        String rname = urn.getResourceName().toString();
        String[] parts = rname.split("x");
        if (parts.length < 2) {
            throw new IOException("Failed to load font: does not contain WxH part in filename!");
        }

        try (InputStream stream = inputs.get(0).openStream()) {
            OCFont font = new OCFont(new String(KComputersUtil.toByteArray(stream), Charsets.UTF_8),
                    Integer.parseInt(parts[parts.length - 1]));

            return new HexFontData(font);
        } catch (IOException e) {
            throw new IOException("Failed to load font: " + e.getMessage(), e);
        }
    }

}
