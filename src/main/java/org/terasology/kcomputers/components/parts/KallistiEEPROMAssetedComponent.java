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
package org.terasology.kcomputers.components.parts;

import org.terasology.engine.entitySystem.Component;

/**
 * Component for a "OpenComputers EEPROM"-style Kallisti peripheral
 * which is sourced from an asset and thus read-only.
 */
public class KallistiEEPROMAssetedComponent implements Component {
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
}
