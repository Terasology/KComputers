// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;

import java.util.ArrayList;

/**
 * Component providing a Kallisti-compatible keyboard.
 */
public class KallistiKeyboardComponent implements Component, KeyboardInputProvider {
    private final transient ArrayList<Key> keyQueue = new ArrayList<>();

    @Override
    public boolean hasNextKey() {
        return !keyQueue.isEmpty();
    }

    @Override
    public Key nextKey() {
        return keyQueue.remove(0);
    }

    /**
     * Add a key to the queue.
     *
     * @param key The key to add.
     */
    public void addKey(Key key) {
        if (key.getCode() != 0) {
            keyQueue.add(key);
        }
    }
}
