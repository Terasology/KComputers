// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.kcomputers.components;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility component used for holding a collection of mesh renderers.
 *
 * This is a bit of a workaround for directly missing functionality
 * in the Terasology engine as of writing. It should probably be replaced
 * with an in-engine solution for achieving the same thing
 * (dynamic mesh rendering in a block space).
 *
 * Generally, the intended use is to have unique keys for each
 * mesh entity (MeshComponent) you wish to render, and manage those
 * using the provided methods.
 */
public class MeshRenderComponent implements Component<MeshRenderComponent> {
    private final Map<String, EntityRef> meshes = new HashMap<>();

    /**
     * Dispose of a given mesh entity and its sub-elements.
     *
     * @param ref The EntityRef to dispose of.
     * @return True if the EntityRef was disposed of.
     */
    private boolean dispose(EntityRef ref) {
        if (ref != null) {
            if (ref.getComponent(MeshComponent.class) != null) {
                ref.getComponent(MeshComponent.class).mesh.dispose();
                ref.getComponent(MeshComponent.class).material.dispose();
            }
            ref.destroy();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove a mesh entity with a given key.
     *
     * @param key The key of the mesh entity.
     * @return Whether or not the removal was successful.
     */
    public boolean remove(String key) {
        return dispose(meshes.remove(key));
    }

    /**
     * Check if a mesh entity with a given key is present.
     *
     * @param key The key of the mesh entity.
     * @return True if the mesh entity is present, false otherwise.
     */
    public boolean has(String key) {
        return meshes.containsKey(key);
    }

    /**
     * Get the MeshComponent of a mesh entity with a given key.
     *
     * @param key The key of the mesh entity.
     * @return The MeshComponent by the given key, or null if no such key is present.
     */
    public MeshComponent get(String key) {
        EntityRef ref = meshes.get(key);
        return ref != null ? ref.getComponent(MeshComponent.class) : null;
    }

    /**
     * Add a new mesh entity.
     * <p>
     * TODO: Currently, this will always clear the existing mesh entity. While sufficient for KallistiDisplayComponent
     * usage, it should probably be improved depending on how well the engine reacts to MeshComponent changes.
     *
     * @param manager An EntityManager instance.
     * @param key The key used for the mesh entity.
     * @param location The location of the mesh entity.
     * @param component The desired MeshComponent.
     * @return True if the addition was successful, false otherwise.
     */
    public boolean add(EntityManager manager, String key, Vector3f location, MeshComponent component) {
        EntityRef ref = meshes.get(key);
        if (ref != null) {
            if (remove(key)) {
                ref = null;
            } else {
                return false;
            }
        }

        EntityBuilder builder = manager.newBuilder();
        builder.setPersistent(false);
        builder.addComponent(new LocationComponent(location));
        builder.addComponent(component);
        ref = builder.build();
        meshes.put(key, ref);
        return true;
    }

    public void clear() {
        for (EntityRef ref : meshes.values()) {
            dispose(ref);
        }
        meshes.clear();
    }
}
