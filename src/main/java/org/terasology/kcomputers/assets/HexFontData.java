// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.assets;

import org.terasology.gestalt.assets.AssetData;
import org.terasology.kallisti.oc.OCFont;

public class HexFontData implements AssetData {
    private final OCFont font;

    public HexFontData(OCFont font) {
        this.font = font;
    }

    public OCFont getFont() {
        return font;
    }
}
