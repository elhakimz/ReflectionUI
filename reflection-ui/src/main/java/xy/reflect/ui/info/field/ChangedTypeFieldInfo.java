/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.info.field;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.ReflectionUIError;

public class ChangedTypeFieldInfo extends FieldInfoProxy {

	protected ITypeInfo newType;
	protected Filter<Object> conversionMethod;
	protected Filter<Object> reverseConversionMethod;
	protected boolean nullValueConverted;

	public ChangedTypeFieldInfo(IFieldInfo base, ITypeInfo newType, Filter<Object> conversionMethod,
			Filter<Object> reverseConversionMethod, boolean nullValueConverted) {
		super(base);
		this.newType = newType;
		this.conversionMethod = conversionMethod;
		this.reverseConversionMethod = reverseConversionMethod;
		this.nullValueConverted = nullValueConverted;
	}

	protected Object convert(Object value) {
		if (conversionMethod == null) {
			return value;
		}
		if (value == null) {
			if (!nullValueConverted) {
				return null;
			}
		}
		try {
			return conversionMethod.get(value);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	protected Object revertConversion(Object value) {
		if (reverseConversionMethod == null) {
			return value;
		}
		if (value == null) {
			return null;
		}
		try {
			return reverseConversionMethod.get(value);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		Object result = convert(value);
		return result;
	}

	@Override
	public void setValue(Object object, Object value) {
		super.setValue(object, revertConversion(value));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		return super.getNextUpdateCustomUndoJob(object, revertConversion(newValue));
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object[] result = super.getValueOptions(object);
		if (result == null) {
			return null;
		}
		Object[] convertedResult = new Object[result.length];
		for (int i = 0; i < result.length; i++) {
			convertedResult[i] = convert(result[i]);
		}
		return convertedResult;
	}

	@Override
	public ITypeInfo getType() {
		return newType;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.CALCULATED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((conversionMethod == null) ? 0 : conversionMethod.hashCode());
		result = prime * result + ((newType == null) ? 0 : newType.hashCode());
		result = prime * result + ((reverseConversionMethod == null) ? 0 : reverseConversionMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangedTypeFieldInfo other = (ChangedTypeFieldInfo) obj;
		if (conversionMethod == null) {
			if (other.conversionMethod != null)
				return false;
		} else if (!conversionMethod.equals(other.conversionMethod))
			return false;
		if (newType == null) {
			if (other.newType != null)
				return false;
		} else if (!newType.equals(other.newType))
			return false;
		if (reverseConversionMethod == null) {
			if (other.reverseConversionMethod != null)
				return false;
		} else if (!reverseConversionMethod.equals(other.reverseConversionMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChangedTypeField [newType=" + newType + ", conversionMethod=" + conversionMethod
				+ ", reverseConversionMethod=" + reverseConversionMethod + "]";
	}

}
