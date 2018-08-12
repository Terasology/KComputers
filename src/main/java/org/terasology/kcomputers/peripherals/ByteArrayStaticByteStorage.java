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
package org.terasology.kcomputers.peripherals;

import org.terasology.kallisti.base.interfaces.StaticByteStorage;

/**
 * Simple byte array-based implementation of StaticByteStorage.
 *
 * While it supports writes (OpenComputers's BIOS may have issues booting
 * off a 100% read-only EEPROM), they are not stored anywhere.
 *
 * TODO: Make the code and data of the EEPROM stored in the component,
 * removing the need for this kludge.
 */
public class ByteArrayStaticByteStorage implements StaticByteStorage {
    private final byte[] data;

    public ByteArrayStaticByteStorage(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] get() {
        return data;
    }

    @Override
    public boolean canModify() {
        return true;
    }

    @Override
    public void markModified() {
        // TODO
    }
}
