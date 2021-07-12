// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components.parts;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Component for a "OpenComputers EEPROM"-style Kallisti peripheral
 * which is sourced from an asset and thus read-only.
 */
public class KallistiEEPROMAssetedComponent implements Component<KallistiEEPROMAssetedComponent> {
    /**
     * The name of the .ZIP file from which to extract the
     * EEPROM file.
     *
     * @see org.terasology.kcomputers.assets.KallistiArchive
     */
    public String assetName;

    /**
     * The name of the file to read as the EEPROM.
     */
    public String filename;

    @Override
    public void copy(KallistiEEPROMAssetedComponent other) {
        this.assetName = other.assetName;
        this.filename = other.filename;
    }
}
