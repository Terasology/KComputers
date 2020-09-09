// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.peripherals;

import org.terasology.kallisti.base.interfaces.StaticByteStorage;

/**
 * Simple byte array-based implementation of StaticByteStorage.
 * <p>
 * While it supports writes (OpenComputers's BIOS may have issues booting off a 100% read-only EEPROM), they are not
 * stored anywhere.
 * <p>
 * TODO: Make the code and data of the EEPROM stored in the component, removing the need for this kludge.
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
