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
package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class MethodInfoProxy extends AbstractInfoProxy implements IMethodInfo {

	protected IMethodInfo base;

	public MethodInfoProxy(IMethodInfo base) {
		this.base = base;
	}

	public IMethodInfo getBase() {
		return base;
	}

	@Override
	public String getSignature() {
		return base.getSignature();
	}

	public boolean isHidden() {
		return base.isHidden();
	}

	public void onControlVisibilityChange(Object object, boolean visible) {
		base.onControlVisibilityChange(object, visible);
	}

	public boolean isNullReturnValueDistinct() {
		return base.isNullReturnValueDistinct();
	}

	public boolean isReturnValueDetached() {
		return base.isReturnValueDetached();
	}

	public boolean isReturnValueIgnored() {
		return base.isReturnValueIgnored();
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(Object object, InvocationData invocationData) {
		return base.invoke(object, invocationData);
	}

	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return base.getNextInvocationUndoJob(object, invocationData);
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		base.validateParameters(object, invocationData);
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return base.getConfirmationMessage(object, invocationData);
	}

	public String getParametersValidationCustomCaption() {
		return base.getParametersValidationCustomCaption();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		MethodInfoProxy other = (MethodInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodInfoProxy [signature=" + getSignature() + ", base=" + base + "]";
	}

}
