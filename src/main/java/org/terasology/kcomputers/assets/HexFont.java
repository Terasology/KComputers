// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.assets;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetFactory;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetType;
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

    private HexFontData data;

    public HexFont(ResourceUrn urn, AssetType<?, HexFontData> assetType, HexFontData data) {
        super(urn, assetType);
        reload(data);
    }

    /**
     * Get the font in a Kallisti-usable format.
     *
     * @return The OCFont instance.
     */
    public OCFont getKallistiFont() {
        return data.getFont();
    }

    @Override
    protected void doReload(HexFontData data) {
        this.data = data;
    }

    public static class Factory implements AssetFactory<HexFont, HexFontData> {
        @Override
        public HexFont build(ResourceUrn urn, AssetType<HexFont, HexFontData> assetType, HexFontData data) {
            return new HexFont(urn, assetType, data);
        }
    }

}
