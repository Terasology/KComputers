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

import org.terasology.entitySystem.Component;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kallisti.base.util.Dimension;
import org.terasology.kallisti.base.util.KallistiFileUtils;
import org.terasology.kallisti.oc.OCFont;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.OCTextRenderer;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class KallistiDisplayComponent implements Component, FrameBuffer, KallistiComponentContainer {
	private transient Synchronizable source;
	private transient Renderer renderer;

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

	@Override
	public void blit(Image image) {
		System.out.println("Received blittable image OwO");
	}

	public void render() {
		if (renderer == null) {
			try {
				renderer = new OCGPURenderer(
						new OCTextRenderer(
								new OCFont(
										KallistiFileUtils.readString(
												new File("/home/asie/Kallisti/funscii-16.hex")
										),
										16
								)
						)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Collection<Object> getKallistiComponents() {
		return Collections.singleton(this);
	}
}
