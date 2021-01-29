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
package org.terasology.kcomputers.systems;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.PeripheralOCGPU;
import org.terasology.kcomputers.assets.KallistiArchive;
import org.terasology.kcomputers.components.machines.KallistiTransposerComponent;
import org.terasology.kcomputers.components.parts.KallistiEEPROMAssetedComponent;
import org.terasology.kcomputers.components.parts.KallistiFilesystemAssetedComponent;
import org.terasology.kcomputers.components.parts.KallistiMachineOpenComputersComponent;
import org.terasology.kcomputers.components.parts.KallistiMemoryComponent;
import org.terasology.kcomputers.components.parts.KallistiOpenComputersGPUComponent;
import org.terasology.kcomputers.events.KallistiAttachComponentsEvent;
import org.terasology.kcomputers.events.KallistiRegisterComponentRulesEvent;
import org.terasology.kcomputers.peripherals.ByteArrayStaticByteStorage;
import org.terasology.kcomputers.peripherals.PeripheralTransposer;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

/**
 * This system creates Kallisti component objects for KComputers-added
 * Terasology components.
 *
 * @see KallistiAttachComponentsEvent
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class KallistiPeripheralSystem extends BaseComponentSystem {
    @In
    private WorldProvider provider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private InventoryManager inventoryManager;

    @ReceiveEvent
    public void createTransposerPeripheral(KallistiAttachComponentsEvent event, EntityRef ref, BlockComponent block, KallistiTransposerComponent component) {
        event.addComponent(ref, new PeripheralTransposer(provider, blockEntityRegistry, ref, block, inventoryManager));
    }

    @ReceiveEvent
    public void createEEPROMAssetedComponent(KallistiAttachComponentsEvent event, EntityRef ref, KallistiEEPROMAssetedComponent component) {
        KallistiArchive archive = CoreRegistry.get(AssetManager.class)
                .getAsset(new ResourceUrn(component.assetName), KallistiArchive.class)
                .get();

        byte[] biosEepromCode = archive.getData().readFully(component.filename);
        int dataSize = 256;

        // The OpenComputers EEPROM keeps 256 bytes of "data" at the end.
        byte[] biosEeprom = new byte[biosEepromCode.length + dataSize];
        System.arraycopy(biosEepromCode, 0, biosEeprom, 0, biosEepromCode.length);

        event.addComponent(ref, new ByteArrayStaticByteStorage(biosEeprom));
    }

    @ReceiveEvent
    public void createFilesystemAssetedComponent(KallistiAttachComponentsEvent event, EntityRef ref, KallistiFilesystemAssetedComponent component) {
        KallistiArchive archive = CoreRegistry.get(AssetManager.class)
                .getAsset(new ResourceUrn(component.assetName), KallistiArchive.class)
                .get();

        event.addComponent(ref, archive.getData());
    }

    @ReceiveEvent
    public void createMachineOpenComputersComponent(KallistiAttachComponentsEvent event, EntityRef ref, KallistiMachineOpenComputersComponent component) {
        event.addComponent(ref, component);
    }

    @ReceiveEvent
    public void createMemoryComponent(KallistiAttachComponentsEvent event, EntityRef ref, KallistiMemoryComponent component) {
        if (ref.hasComponent(ItemComponent.class)) {
            int stackCount = ref.getComponent(ItemComponent.class).stackCount;
            if (stackCount > 1) {
                KallistiMemoryComponent combinedComponent = new KallistiMemoryComponent();
                combinedComponent.amount = component.amount * stackCount;
                event.addComponent(ref, combinedComponent);
                return;
            }
        }

        event.addComponent(ref, component);
    }

    @ReceiveEvent
    public void createOCGPUComponent(KallistiAttachComponentsEvent event, EntityRef ref, KallistiOpenComputersGPUComponent component) {
        event.addComponent(ref, component);
    }

    @ReceiveEvent
    public void registerOCRules(KallistiRegisterComponentRulesEvent event, EntityRef ref) {
        event.registerRules(this);
    }

    @ComponentRule
    public static PeripheralOCGPU createGPUFromComponent(MachineOpenComputers machine, KallistiOpenComputersGPUComponent component) {
        return new PeripheralOCGPU(machine, component.width, component.height, OCGPURenderer.genThirdTierPalette());
    }
}
