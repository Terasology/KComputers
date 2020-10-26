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
package org.terasology.kcomputers.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.kallisti.base.component.Machine;

/**
 * Event used for registering classes and objects with @ComponentRule
 * methods.
 *
 * @see org.terasology.kallisti.base.component.ComponentRule
 */
public class KallistiRegisterComponentRulesEvent implements Event {
    private transient final Machine machine;

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
