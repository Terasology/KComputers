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
import org.terasology.kallisti.oc.OCFont;

/**
 * Asset type used for creating Kallisti-ready OCFont objects from
 * ".hex"-format fonts.
 *
 * It is imperative that the font files be named in the style of
 * "[name]-[width]x[height].hex", for example "unicode-8x16.hex" for
 * an 8x16-glyph font file.
 *
 * See the documentation for OCFont for more information on the format.
 */
@RegisterAssetType(factoryClass = HexFont.Factory.class, folderName = "fonts")
public class HexFont extends Asset<HexFontData> {
    public static class Factory implements AssetFactory<HexFont, HexFontData> {
        @Override
        public HexFont build(ResourceUrn urn, AssetType<HexFont, HexFontData> assetType, HexFontData data) {
            return new HexFont(urn, assetType, data);
        }
    }

    private HexFontData data;

    public HexFont(ResourceUrn urn, AssetType<?, HexFontData> assetType, HexFontData data) {
        super(urn, assetType);
        reload(data);
    }

    /**
     * Get the font in a Kallisti-usable format.
     * @return The OCFont instance.
     */
    public OCFont getKallistiFont() {
        return data.getFont();
    }

    @Override
    protected void doReload(HexFontData data) {
        this.data = data;
    }
}
