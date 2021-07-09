// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components.parts;

import com.google.common.base.Charsets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaState52;
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kcomputers.assets.HexFont;
import org.terasology.kcomputers.assets.KallistiArchive;
import org.terasology.kcomputers.components.KallistiMachineProvider;

/**
 * Component for an OpenComputers-style machine provider.
 */
public class KallistiMachineOpenComputersComponent implements Component<KallistiMachineOpenComputersComponent>, KallistiMachineProvider {
    /**
     * The version of Lua to use for this machine provider, in the form
     * MAJOR.MINOR (for example "5.2" or "5.3").
     *
     * @see org.terasology.jnlua.LuaState
     */
    public String luaVersion;

    @Override
    public Machine create(ComponentContext kallistiContext, EntityRef computerEntity, EntityRef providerEntity, int memorySize) {
        Class<? extends LuaState> luaClass;

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
                memorySize, false, luaClass, false
        );

        return machineOpenComputers;
    }
}
