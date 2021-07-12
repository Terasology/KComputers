// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.kallisti.base.interfaces.Synchronizable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Component provided by blocks which
 */
public class KallistiDisplayCandidateComponent implements Component<KallistiDisplayCandidateComponent>, Synchronizable.Receiver {
    /**
     * The thickness of the texture's border, used to make the display
     * not render over it, in blocks.
     */
    public float borderThickness = 1 / 16f;

    /**
     * The maximum width of a display, in blocks.
     *
     * TODO: Implement
     */
    public int maxWidth = 8;

    /**
     * The maximum height of a display, in blocks.
     *
     * TODO: Implement
     */
    public int maxHeight = 8;

    /**
     * If true, this KallistiDisplayCandidate will try to form a
     * multiblock display.
     *
     * TODO: Implement
     */
    public boolean multiBlock = false;

    private transient KallistiDisplayComponent display;

    /**
     * Get the KallistiDisplayComponent instance.
     * @return The KallistiDisplayComponent instance.
     */
    public KallistiDisplayComponent getDisplay() {
        return display;
    }

    @Override
    public void update(InputStream stream) throws IOException {
        display.update(stream);
    }

    /**
     * Set the KallistiDisplayComponent instance.
     * @param displayComponent The KallistiDisplayComponent instance.
     */
    public void setDisplay(KallistiDisplayComponent displayComponent) {
        this.display = displayComponent;
    }

    @Override
    public void copy(KallistiDisplayCandidateComponent other) {
        this.borderThickness = other.borderThickness;
        this.maxWidth = other.maxWidth;
        this.maxHeight = other.maxHeight;
        this.multiBlock = other.multiBlock;
        this.display = other.display;
    }
}
