// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.events;

import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.kallisti.base.component.Machine;

/**
 * Event used for registering classes and objects with @ComponentRule
 * methods.
 *
 * @see org.terasology.kallisti.base.component.ComponentRule
 */
public class KallistiRegisterComponentRulesEvent implements Event {
    private final transient Machine machine;

    public KallistiRegisterComponentRulesEvent(Machine machine) {
        this.machine = machine;
    }

    /**
     * Register all @ComponentRule-annotated methods provided by a given class.
     *
     * @param c The class.
     */
    public void registerRules(Class c) {
        machine.registerRules(c);
    }

    /**
     * Register all @ComponentRule-annotated methods provided by a given object.
     *
     * @param o The object.
     */
    public void registerRules(Object o) {
        machine.registerRules(o);
    }
}
