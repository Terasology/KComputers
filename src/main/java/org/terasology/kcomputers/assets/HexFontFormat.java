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

import com.google.common.base.Charsets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
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
