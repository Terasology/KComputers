// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
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
import org.terasology.module.inventory.systems.InventoryManager;

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
    public void createTransposerPeripheral(KallistiAttachComponentsEvent event,
                                           EntityRef ref,
                                           BlockComponent block,
                                           KallistiTransposerComponent component) {
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
    public void createMachineOpenComputersComponent(KallistiAttachComponentsEvent event,
                                                    EntityRef ref,
                                                    KallistiMachineOpenComputersComponent component) {
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
