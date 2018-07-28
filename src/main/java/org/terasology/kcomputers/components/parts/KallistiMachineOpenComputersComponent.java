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

import com.google.common.base.Charsets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kcomputers.components.KallistiMachineProvider;
import org.terasology.kcomputers.kallisti.HexFont;
import org.terasology.kcomputers.kallisti.KallistiArchive;
import org.terasology.registry.CoreRegistry;

public class KallistiMachineOpenComputersComponent implements Component, KallistiMachineProvider {
    @Override
    public Machine create(ComponentContext kallistiContext, EntityRef computerEntity, EntityRef providerEntity) {
        KallistiArchive ocFiles = CoreRegistry.get(AssetManager.class)
                .getAsset(new ResourceUrn("KComputers:opencomputers"), KallistiArchive.class)
                .get();

        MachineOpenComputers machineOpenComputers = new MachineOpenComputers(
                ocFiles.getData().readFully("machine.lua", Charsets.UTF_8),
                kallistiContext,
                CoreRegistry.get(AssetManager.class)
                        .getAsset(new ResourceUrn("KComputers:unicode-8x16"), HexFont.class)
                        .get().getKallistiFont(),
                1048576, LuaState53.class, false
        );

        machineOpenComputers.registerRules(KallistiOpenComputersGPUComponent.class);

        return machineOpenComputers;
    }
}
