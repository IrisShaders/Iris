/*
 *    This file is part of the Distant Horizons mod
 *    licensed under the GNU LGPL v3 License.
 *
 *    Copyright (C) 2020-2023 James Seibel
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

package com.seibel.distanthorizons.api.interfaces.override.rendering;

/**
 * The culling frustum used during Distant Horizons' shadow pass
 * if another mod has enabled Distant Horizons' shadow
 * pass via the API. <br><br>
 * 
 * If no {@link IDhApiShadowCullingFrustum} is bound then culling
 * will not be done in the shadow pass.
 * 
 * @see IDhApiCullingFrustum
 * 
 * @author James Seibel
 * @version 2024-2-10
 * @since API 2.0.0
 */
public interface IDhApiShadowCullingFrustum extends IDhApiCullingFrustum
{
	// should be identical to the parent culling frustum
}
