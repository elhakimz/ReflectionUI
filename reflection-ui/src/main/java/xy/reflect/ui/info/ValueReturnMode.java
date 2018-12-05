/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.info;

import java.util.Arrays;
import java.util.Collections;

/**
 * This enumeration allows to specify what kind of relation exists between a
 * value returned by a class member (field or a method) and its source object.
 * 
 * @author olitank
 *
 */
public enum ValueReturnMode {

	/**
	 * Means that the value is a reference or a proxy of a value stored in the
	 * source object. Thus altering the value will alter the source object.
	 */
	DIRECT_OR_PROXY,

	/**
	 * Means that the value is not stored in the source object. It is either a copy
	 * or a calculation result. Thus altering the value will not alter the source
	 * object.
	 */
	CALCULATED,

	/**
	 * Means that the value could be stored in the source object or not. Thus
	 * altering the could alter or not the source object.
	 */
	INDETERMINATE;

	/**
	 * 
	 * @param parent
	 *            The parent return mode.
	 * @param child
	 *            The child return mode.
	 * @return the result of the combination of 2 overlaid value return modes.
	 */
	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		return Collections.max(Arrays.asList(parent, child));
	}
}
