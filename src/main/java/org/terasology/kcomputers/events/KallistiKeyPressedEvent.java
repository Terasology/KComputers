// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;

/**
 * This event is sent from the client to the server upon a KallistiKeyboard
 * key press.
 *
 * @see KeyboardInputProvider.Key
 */
@ServerEvent
public class KallistiKeyPressedEvent extends NetworkEvent {
    private int type;
    private int code;
    private int chr;

    public KallistiKeyPressedEvent() {
        type = 0;
        code = 0;
        chr = 0;
    }

    public KallistiKeyPressedEvent(KeyboardInputProvider.Key key) {
        type = key.getType().ordinal();
        code = key.getCode();
        chr = key.getChar();
    }

    /**
     * Get a KeyboardInputProvider.Key instance, usable in the context of a Kallisti virtual machine.
     *
     * @return The instance.
     */
    public KeyboardInputProvider.Key getKey() {
        return new KeyboardInputProvider.Key(
            KeyboardInputProvider.KeyType.values()[type],
            code,
            chr
        );
    }
}
