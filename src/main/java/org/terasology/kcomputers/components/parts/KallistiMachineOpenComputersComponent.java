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
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaState52;
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kcomputers.components.KallistiComponentContainer;
import org.terasology.kcomputers.components.KallistiMachineProvider;
import org.terasology.kcomputers.kallisti.HexFont;
import org.terasology.kcomputers.kallisti.KallistiArchive;
import org.terasology.registry.CoreRegistry;

import java.util.Collection;
import java.util.Collections;

public class KallistiMachineOpenComputersComponent implements Component, KallistiComponentContainer, KallistiMachineProvider {
    public String luaVersion;

    @Override
    public Machine create(ComponentContext kallistiContext, EntityRef computerEntity, EntityRef providerEntity, int memorySize) {
        Class<? extends LuaState> luaClass;

        // TODO: multiply memory size by ~1.75 only on 64-bit architectures
        memorySize = Math.round(memorySize * 1.75f);

        if ("5.2".equals(luaVersion)) {
            luaClass = LuaState52.class;
        } else if ("5.3".equals(luaVersion)) {
            luaClass = LuaState53.class;
        } else {
            throw new RuntimeException("Unknown Lua version: " + (luaVersion != null ? luaVersion : "null") + "!");
        }

        KallistiArchive ocFiles = CoreRegistry.get(AssetManager.class)
                .getAsset(new ResourceUrn("KComputers:opencomputers"), KallistiArchive.class)
                .get();

        MachineOpenComputers machineOpenComputers = new MachineOpenComputers(
                ocFiles.getData().readFully("machine.lua", Charsets.UTF_8),
                kallistiContext,
                CoreRegistry.get(AssetManager.class)
                        .getAsset(new ResourceUrn("KComputers:unicode-8x16"), HexFont.class)
                        .get().getKallistiFont(),
                memorySize, luaClass, false
        );

        machineOpenComputers.registerRules(KallistiOpenComputersGPUComponent.class);

        return machineOpenComputers;
    }

    @Override
    public Collection<Object> getKallistiComponents(EntityRef entity) {
        return Collections.singleton(this);
    }
}
