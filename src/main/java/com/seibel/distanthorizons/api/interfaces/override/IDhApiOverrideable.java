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

package com.seibel.distanthorizons.api.interfaces.override;

import com.seibel.distanthorizons.coreapi.interfaces.dependencyInjection.IBindable;
import com.seibel.distanthorizons.coreapi.interfaces.dependencyInjection.IOverrideInjector;

/**
 * Implemented by all DhApi objects that can be overridden.
 *
 * @author James Seibel
 * @version 2022-9-5
 * @since API 1.0.0
 */
public interface IDhApiOverrideable extends IBindable {
	/**
	 * Higher (larger numerical) priorities override lower (smaller numerical) priorities. <br>
	 * For most developers this can be left at the default.
	 *
	 * @return The priority of this interface, the lowest legal value is {@link IOverrideInjector#MIN_NON_CORE_OVERRIDE_PRIORITY}.
	 */
	default int getPriority() {
		return IOverrideInjector.DEFAULT_NON_CORE_OVERRIDE_PRIORITY;
	}

}
