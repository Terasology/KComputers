// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components.parts;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Component for a Kallisti filesystem peripheral which is sourced
 * from an asset and thus read-only.
 */
public class KallistiFilesystemAssetedComponent implements Component<KallistiFilesystemAssetedComponent> {
    /**
     * The name of the .ZIP filesystem to use as the virtual machine's
     * filesystem.
     *
     * @see org.terasology.kcomputers.assets.KallistiArchive
     */
    public String assetName;

    @Override
    public void copy(KallistiFilesystemAssetedComponent other) {
        this.assetName = other.assetName;
    }
}
