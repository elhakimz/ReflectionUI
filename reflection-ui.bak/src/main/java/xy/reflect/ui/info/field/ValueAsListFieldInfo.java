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
package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

public class ValueAsListFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public ValueAsListFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo containingType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(new StandardCollectionTypeInfo(reflectionUI,
					new JavaTypeInfoSource(ArrayList.class, null), super.getType()) {

				@Override
				public boolean isInsertionAllowed() {
					return false;
				}

				@Override
				public boolean isRemovalAllowed() {
					return false;
				}

				@Override
				public boolean canReplaceContent() {
					return false;
				}

				@Override
				public boolean canInstanciateFromArray() {
					return true;
				}

			}, new SpecificitiesIdentifier(containingType.getName(), ValueAsListFieldInfo.this.getName())));
		}
		return type;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getValue(Object object) {
		return new ArrayList(Arrays.asList(super.getValue(object)));
	}

	@Override
	public void setValue(Object object, Object value) {
		value = ((List<?>) value).get(0);
		super.setValue(object, value);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public String toString() {
		return "ValueAsListField [base=" + base + "]";
	}

}
