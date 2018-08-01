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

import com.google.common.collect.ImmutableSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.component.ComponentInterface;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.PeripheralOCGPU;

import java.util.Collection;

@ComponentInterface
public class KallistiOpenComputersGPUComponent implements Component {
    public int width = 160;
    public int height = 50;
}
