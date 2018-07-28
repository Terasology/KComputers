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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.interfaces.Synchronizable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class KallistiDisplayCandidateComponent implements Component, Synchronizable.Receiver, KallistiComponentContainer {
    public float borderThickness = 1 / 16f;
    public int maxDimension = 8;
    public boolean multiBlock = false;

    private transient KallistiDisplayComponent display;

    public KallistiDisplayComponent getDisplay() {
        return display;
    }

    @Override
    public void update(InputStream stream) throws IOException {
        display.update(stream);
    }

    @Override
    public Collection<Object> getKallistiComponents(EntityRef entity) {
        return display.getKallistiComponents(entity);
    }

    public void setDisplay(KallistiDisplayComponent displayComponent) {
        this.display = displayComponent;
    }
}
