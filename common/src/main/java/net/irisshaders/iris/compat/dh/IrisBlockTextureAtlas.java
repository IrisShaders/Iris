
/*
 *    This file is part of the Distant Horizons mod
 *    licensed under the GNU LGPL v3 License.
 *
 *    Copyright (C) 2020 James Seibel
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, version 3.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiBlockTextureAtlas;
import com.seibel.distanthorizons.coreapi.util.BitShiftUtil;
import com.seibel.distanthorizons.coreapi.util.TextureUtil;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * The Blaze3D equivalent of the OpenGL block texture atlas. <br><br>
 *
 * Blaze3D doesn't support array textures yet, so unlike the OpenGL atlas
 * the tiles are packed into a 2D grid texture
 * with {@link DhBlockTextureAtlasUploadHandler#TILES_PER_ROW} tiles per row,
 * growing downward as more tiles are registered.
 * The fragment shader locates a tile via its id and the texture's size,
 * so no extra uniforms are needed.
 *
 * @see BlockTextureRegistry
 * @see com.seibel.distanthorizons.common.render.openGl.terrain.GlBlockTextureAtlas
 */
public class IrisBlockTextureAtlas implements IDhApiBlockTextureAtlas
{
	public static final IrisBlockTextureAtlas INSTANCE = new IrisBlockTextureAtlas();

	private GpuTexture texture;
	private GpuTextureView view;



	//=============//
	// constructor //
	//=============//
	//region

	private IrisBlockTextureAtlas() { }

	//endregion



	//==================//
	// texture handling //
	//==================//
	//region

	@Override
	public void tryCreateOrResize(int width, int height) {
		if (texture != null && texture.getWidth(0) == width && texture.getHeight(0) == height) {
			return;
		}
		if (texture != null) {
			view.close();
			texture.close();
		}

		int mipLevelCount = (int)Math.sqrt(16);
		mipLevelCount += 1;

		texture = RenderSystem.getDevice().createTexture("DH Atlas (Iris)", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
			GpuFormat.RGBA8_UNORM, width, height, 1,mipLevelCount );
		view = RenderSystem.getDevice().createTextureView(texture);
	}

	//endregion



	//===========//
	// uploading //
	//===========//
	//region

	@Override
	public void beforeWriteToTexture() { /* no setup required */ }

	@Override
	public void writeToTexture(ByteBuffer pixelBuffer, int destinationX, int destinationY, int tileWidth, int tileHeight)
	{
		if (tileWidth != tileHeight)
		{
			throw new IllegalArgumentException("Mip maps require textures be a square, width: ["+tileWidth+"], height: ["+tileHeight+"].");
		}


		int size = TextureUtil.TEXTURE_WIDTH_AND_HEIGHT;
		int mipLevel = 0;
		ByteBuffer currentLevelData = pixelBuffer;

		try
		{
			do
			{
				RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture,
					currentLevelData,
					mipLevel,
					0,
					BitShiftUtil.divideByPowerOfTwo(destinationX, mipLevel), // u, halved per mip level
					BitShiftUtil.divideByPowerOfTwo(destinationY, mipLevel), // v, halved per mip level
					size, // width
					size  // height
				);

				if (size > 1)
				{
					ByteBuffer nextLevelData = this.generateNextMipLevel(currentLevelData, size);

					if (currentLevelData != pixelBuffer)
					{
						MemoryUtil.memFree(currentLevelData);
					}

					currentLevelData = nextLevelData;
				}

				size /= 2;
				mipLevel++;
			}
			while (size >= 1);
		}
		finally
		{
			if (currentLevelData != pixelBuffer)
			{
				MemoryUtil.memFree(currentLevelData);
			}
		}
	}
	/**
	 * Generates the next mip level by averaging each 2x2 block of RGBA pixels
	 * from the source buffer into a single pixel in the destination buffer.
	 *
	 * @param sourceData source pixel buffer at the current mip size
	 * @param sourceSize width/height of the source mip level (square, power of two)
	 * @return a new ByteBuffer containing the downsampled (sourceSize / 2) mip level
	 */
	private ByteBuffer generateNextMipLevel(ByteBuffer sourceData, int sourceSize)
	{
		// mip maps require the texture to be a square
		int textureWidthHeight = sourceSize / 2;
		ByteBuffer mipData = MemoryUtil.memAlloc(textureWidthHeight * textureWidthHeight * 4);

		for (int v = 0; v < textureWidthHeight; v++)
		{
			for (int u = 0; u < textureWidthHeight; u++)
			{
				int sourceU = u * 2;
				int sourceV = v * 2;

				// get the average of the 4 adjacent pixels
				int index00 = ((sourceV * sourceSize) + sourceU) * 4;
				int index10 = ((sourceV * sourceSize) + (sourceU + 1)) * 4;
				int index01 = (((sourceV + 1) * sourceSize) + sourceU) * 4;
				int index11 = (((sourceV + 1) * sourceSize) + (sourceU + 1)) * 4;

				// Handle each ARGB channel separately.
				// We don't care about which channel we're averaging at the time,
				// they're all handled the same.
				for (int channelIndex = 0; channelIndex < 4; channelIndex++)
				{
					int sum =
						// & 0xFF is to convert from signed byte to unsigned for averaging
						(sourceData.get(index00 + channelIndex) & 0xFF) +
							(sourceData.get(index10 + channelIndex) & 0xFF) +
							(sourceData.get(index01 + channelIndex) & 0xFF) +
							(sourceData.get(index11 + channelIndex) & 0xFF);

					mipData.put((byte) (sum / 4));
				}
			}
		}

		mipData.flip();
		return mipData;
	}

	@Override
	public void afterWriteToTexture() { /* no cleanup required */ }

	public GpuTextureView getView() {
		return view;
	}

	//endregion



}
