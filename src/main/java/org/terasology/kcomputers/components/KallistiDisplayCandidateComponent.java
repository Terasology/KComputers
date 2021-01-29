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
package org.terasology.kcomputers.components;

import org.terasology.entitySystem.Component;
import org.terasology.kallisti.base.interfaces.Synchronizable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Component provided by blocks which
 */
public class KallistiDisplayCandidateComponent implements Component, Synchronizable.Receiver {
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
}
