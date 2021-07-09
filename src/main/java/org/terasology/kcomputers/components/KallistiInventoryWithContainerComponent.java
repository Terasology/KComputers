// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Marker component. If put on an Entity which also contains an
 * InventoryComponent, its item Entities will be accounted for when
 * searching for Kallisti components for a virtual machine.
 */
public class KallistiInventoryWithContainerComponent implements Component<KallistiInventoryWithContainerComponent> {
}
