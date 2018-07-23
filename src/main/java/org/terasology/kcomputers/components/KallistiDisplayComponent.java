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
package org.terasology.kcomputers.components;

import com.google.common.primitives.UnsignedBytes;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kallisti.base.util.Dimension;
import org.terasology.kallisti.base.util.KallistiFileUtils;
import org.terasology.kallisti.oc.OCFont;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.OCTextRenderer;
import org.terasology.kcomputers.KComputersUtil;
import org.terasology.kcomputers.kallisti.HexFont;
import org.terasology.kcomputers.kallisti.HexFontData;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.NoReplicate;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.shapes.BlockMeshPart;
import org.terasology.world.block.shapes.BlockShape;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

/**
 * Component provided by blocks which provide a Kallisti-compatible display.
 */
@NoReplicate
public class KallistiDisplayComponent implements Component, FrameBuffer, Synchronizable.Receiver, KallistiComponentContainer {
	private static final String DISPLAY_KEY = "display";

	private transient EntityManager entityManager;
	private transient EntityRef self;
	private transient KallistiDisplayCandidateComponent candidate;
	private transient MeshRenderComponent mesh;

	public void configure(EntityManager entityManager, EntityRef self, KallistiDisplayCandidateComponent candidate, MeshRenderComponent mesh) {
		this.entityManager = entityManager;
		this.self = self;
		this.candidate = candidate;
		this.mesh = mesh;

		candidate.setDisplay(this);
	}

	private transient Synchronizable source;
	private transient Renderer renderer;
	private transient Texture texture;
	private transient int pw, ph;

	public Synchronizable getSource() {
		return source;
	}

	@Override
	public void bind(Synchronizable source, Renderer renderer) {
		this.source = source;
		this.renderer = renderer;
	}

	@Override
	public Dimension aspectRatio() {
		return new Dimension(1, 1);
	}

	private transient ByteBuffer dataBB;

	@Override
	public void finalize() {
		dataBB.clear();
	}

	@Override
	public void blit(Image image) {
		MeshComponent component;

		if (dataBB == null || dataBB.capacity() != 4 * image.size().getX() * image.size().getY()) {
			if (dataBB != null) {
				dataBB.clear();
			}
			dataBB = ByteBuffer.allocateDirect(4 * image.size().getX() * image.size().getY());
		}

		for (int argb : image.data()) {
			int r = (argb >> 16) & 0xFF;
			int g = (argb >> 8) & 0xFF;
			int b = argb & 0xFF;
			dataBB.put(UnsignedBytes.checkedCast(r));
			dataBB.put(UnsignedBytes.checkedCast(g));
			dataBB.put(UnsignedBytes.checkedCast(b));
			dataBB.put((byte) 0xFF);
		}

		dataBB.rewind();

		pw = image.size().getX();
		ph = image.size().getY();
		texture = Assets.generateAsset(new TextureData(image.size().getX(), image.size().getY(),
			new ByteBuffer[]{ dataBB }, Texture.WrapMode.REPEAT, Texture.FilterMode.NEAREST), Texture.class);

		MaterialData terrainMatData = new MaterialData(Assets.getShader("engine:genericMeshMaterial").get());
		terrainMatData.setParam("diffuse", texture);
		terrainMatData.setParam("colorOffset", new float[]{1, 1, 1});
		terrainMatData.setParam("textured", true);
		Material material = Assets.generateAsset(terrainMatData, Material.class);

		component = mesh.get(DISPLAY_KEY);
		if (component != null) {
			component.material.dispose();
			component.material = material;
		} else {
			component = new MeshComponent();
			component.material = material;

			Vector3f location = self.getComponent(BlockComponent.class).getPosition().toVector3f().add(0.5f, 0.5f, 0.5f);
			Side side = self.getComponent(BlockComponent.class).getBlock().getDirection();
			if (side == null) side = Side.TOP;

			MeshBuilder meshBuilder = new MeshBuilder();
			BlockShape blockShape = Assets.get("engine:cube", BlockShape.class).get();
			BlockMeshPart meshPart = blockShape.getMeshPart(BlockPart.fromSide(side));

			for (int i = 0; i < meshPart.indicesSize(); i++) {
				meshBuilder.addIndices(meshPart.getIndex(i));
			}

			for (int i = 0; i < meshPart.size(); i++) {
				Vector3f v = new Vector3f(meshPart.getVertex(i));
				// reduce by border size
				Vector3f reduction = new Vector3f(
						1 - (candidate.borderThickness * (1 - Math.abs(side.getVector3i().x))),
						1 - (candidate.borderThickness * (1 - Math.abs(side.getVector3i().y))),
						1 - (candidate.borderThickness * (1 - Math.abs(side.getVector3i().z)))
				);

				// bring forward to avoid Z-fighting
				v.mul(reduction.x, reduction.y, reduction.z).add(side.getVector3i().toVector3f().mul(0.01f));

				meshBuilder.addVertex(v.sub(.5f, .5f, .5f));
				meshBuilder.addColor(Color.WHITE);
				meshBuilder.addTexCoord(meshPart.getTexCoord(i));
			}

			component.mesh = meshBuilder.build();
			component.translucent = false;
			component.hideFromOwner = false;
			component.color = Color.WHITE;

			mesh.add(entityManager, DISPLAY_KEY, new Vector3f(location), component);
		}

		self.saveComponent(mesh);
	}

	private void initRenderer() {
		if (renderer == null) {
			try {
				renderer = new OCGPURenderer(
						new OCTextRenderer(
								CoreRegistry.get(AssetManager.class)
								.getAsset(new ResourceUrn("KComputers:unicode-8x16"), HexFont.class)
								.get().getKallistiFont()
						)
				);
			} catch (Exception e) {
				KComputersUtil.LOGGER.warn("Error initializing display renderer!", e);
			}
		}
	}

	public void render() {
		initRenderer();
		renderer.render(this);
	}

	@Override
	public Collection<Object> getKallistiComponents() {
		return Collections.singleton(this);
	}

	@Override
	public void update(InputStream stream) throws IOException {
		initRenderer();

		if (renderer != null) {
			renderer.update(stream);
			render();
		}
	}

	@Nullable
	public Texture getTexture() {
		return texture;
	}

	public int getPixelWidth() {
		return pw;
	}

	public int getPixelHeight() {
		return ph;
	}

	public EntityRef getEntityRef() {
		return self;
	}
}
