// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components.parts;

import org.terasology.engine.entitySystem.Component;
import org.terasology.kallisti.base.component.ComponentInterface;

/**
 * Component providing an OpenComputers-style GPU peripheral.
 */
@ComponentInterface
public class KallistiOpenComputersGPUComponent implements Component {
    /**
     * The maximum (and default) width supported by the GPU, in characters.
     */
    public int width = 160;

    /**
     * The maximum (and default) height supported by the GPU, in characters.
     */
    public int height = 50;
}
