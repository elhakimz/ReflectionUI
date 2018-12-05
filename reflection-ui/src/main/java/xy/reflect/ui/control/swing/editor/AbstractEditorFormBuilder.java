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
package xy.reflect.ui.control.swing.editor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractEditorFormBuilder {

	protected Object initialObjectValue;
	protected boolean objectValueInitialized = false;
	protected boolean objectValueReplaced = false;
	protected Accessor<Object> encapsulatedObjectValueAccessor;

	public abstract SwingRenderer getSwingRenderer();

	public abstract ModificationStack getParentObjectModificationStack();

	public abstract String getCumulatedModificationsTitle();

	public abstract boolean canCommit();

	public abstract IModification createCommitModification(Object newObjectValue);

	public abstract ITypeInfoSource getObjectDeclaredNonSpecificTypeInfoSource();

	public abstract ValueReturnMode getObjectValueReturnMode();

	public abstract boolean isObjectNullValueDistinct();

	public abstract Object getInitialObjectValue();

	public abstract IInfoFilter getObjectFormFilter();

	public abstract boolean isObjectFormExpanded();

	public abstract IContext getContext();

	public abstract IContext getSubContext();

	protected void ensureObjectValueIsInitialized() {
		if (objectValueInitialized) {
			return;
		}
		encapsulatedObjectValueAccessor = new Accessor<Object>() {

			Object object = initialObjectValue = getInitialObjectValue();

			@Override
			public Object get() {
				return object;
			}

			@Override
			public void set(Object t) {
				object = t;
				objectValueReplaced = true;
			}

		};
		objectValueInitialized = true;
	}

	public boolean isObjectValueInitialized() {
		return objectValueInitialized;
	}

	public boolean isObjectValueReplaced() {
		return objectValueReplaced;
	}

	public Object getCurrentObjectValue() {
		ensureObjectValueIsInitialized();
		return encapsulatedObjectValueAccessor.get();
	}

	public Object getEncapsulatedObject() {
		ensureObjectValueIsInitialized();
		return getEncapsulation().getInstance(encapsulatedObjectValueAccessor);
	}

	public boolean canReplaceObjectValue() {
		return canCommit();
	}

	protected String getEncapsulationFieldName() {
		return "";
	}

	protected Map<String, Object> getEncapsulationFieldSpecificProperties() {
		return Collections.emptyMap();
	}

	public String getEncapsulationTypeName() {
		String contextDeclaraion;
		{
			IContext context = getContext();
			if (context == null) {
				contextDeclaraion = "";
			} else {
				contextDeclaraion = "context=" + context.getIdentifier() + ", ";
			}
		}
		String subContextDeclaraion;
		{
			IContext subContext = getSubContext();
			if (subContext == null) {
				subContextDeclaraion = "";
			} else {
				subContextDeclaraion = "subContext=" + subContext.getIdentifier() + ", ";
			}
		}
		return "Encapsulation [" + contextDeclaraion + subContextDeclaraion + "encapsulatedObjectType="
				+ getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldNonSpecificTypeSource())
						.getName()
				+ "]";
	}

	public EncapsulatedObjectFactory getEncapsulation() {
		ITypeInfo fieldType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getEncapsulatedFieldNonSpecificTypeSource());
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getEncapsulationTypeName(), fieldType) {
			@Override
			protected Object[] getFieldValueOptions() {
				return getEncapsulationFieldValueOptions();
			}
		};
		result.setTypeModificationStackAccessible(isEncapsulationTypeModificationStackAccessible());
		result.setTypeCaption(getEncapsulationTypeCaption());
		Map<String, Object> typeSpecificProperties = new HashMap<String, Object>();
		{
			typeSpecificProperties.put(SwingRenderer.CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY,
					!isEncapsulationTypeCustomizationAllowed());
			result.setTypeSpecificProperties(typeSpecificProperties);
		}
		result.setFieldName(getEncapsulationFieldName());
		result.setFieldCaption(getEncapsulatedFieldCaption());
		result.setFieldGetOnly(isEncapsulationFieldGetOnly());
		result.setFieldNullValueDistinct(isObjectNullValueDistinct());
		result.setFieldValueReturnMode(getEncapsulationFieldValueReturnMode());
		result.setFieldFormControlEmbedded(isObjectFormExpanded());
		result.setFieldFormControlFilter(getObjectFormFilter());
		result.setFieldFormControlMandatory(isObjectCustomControlForbidden());
		result.setFieldSpecificProperties(getEncapsulationFieldSpecificProperties());
		return result;
	}

	public boolean isObjectCustomControlForbidden() {
		return false;
	}

	protected ValueReturnMode getEncapsulationFieldValueReturnMode() {
		return hasParentObject() ? getObjectValueReturnMode() : ValueReturnMode.DIRECT_OR_PROXY;
	}

	protected Object[] getEncapsulationFieldValueOptions() {
		return null;
	}

	protected boolean isEncapsulationFieldGetOnly() {
		return isInReadOnlyMode() || !canReplaceObjectValue();
	}

	protected boolean isEncapsulationTypeModificationStackAccessible() {
		return !isInReadOnlyMode();
	}

	protected boolean refreshesEditorFormOnModification() {
		return isInReadOnlyMode();
	}

	protected boolean isInReadOnlyMode() {
		return hasParentObject() ? !canPotentiallyModifyParentObject() : false;
	}

	protected boolean isEncapsulationTypeCustomizationAllowed() {
		return true;
	}

	public String getEncapsulatedFieldCaption() {
		return "";
	}

	public ITypeInfoSource getEncapsulatedFieldNonSpecificTypeSource() {
		ITypeInfoSource result = getObjectDeclaredNonSpecificTypeInfoSource();
		if (result != null) {
			return result;
		}
		ensureObjectValueIsInitialized();
		if (initialObjectValue != null) {
			return getSwingRenderer().getReflectionUI().getTypeInfoSource(initialObjectValue);
		}
		return new JavaTypeInfoSource(Object.class, null);
	}

	public String getEncapsulationTypeCaption() {
		return getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldNonSpecificTypeSource())
				.getCaption();
	}

	public boolean isObjectFormEmpty() {
		Object encapsulatedObject = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsulatedObject));
		IFieldInfo encapsulatedObjectField = encapsulatedObjectType.getFields().get(0);
		if (encapsulatedObjectField.isNullValueDistinct()) {
			return false;
		}
		Object object = getCurrentObjectValue();
		if (object == null) {
			return false;
		}
		ITypeInfo actualObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
		if (!SwingRendererUtils.isFormEmpty(actualObjectType, getObjectFormFilter(), getSwingRenderer())) {
			return false;
		}
		return true;
	}

	public Form createForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
		Object encapsulated = getEncapsulatedObject();
		Form result = getSwingRenderer().createForm(encapsulated);
		setupFormLinkWithparent(result, realTimeLinkWithParent, exclusiveLinkWithParent);
		return result;
	}

	protected void setupFormLinkWithparent(Form result, boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
		if (realTimeLinkWithParent) {
			if (canPotentiallyModifyParentObject()) {
				forwardEditorFormModificationsToParentObject(result, exclusiveLinkWithParent);
			}
			if (refreshesEditorFormOnModification()) {
				refreshEditorFormOnModification(result);
			}
		}
	}

	protected boolean hasParentObject() {
		return getParentObjectModificationStack() != null;
	}

	public boolean canPotentiallyModifyParentObject() {
		if (!hasParentObject()) {
			return false;
		}
		ensureObjectValueIsInitialized();
		return ReflectionUIUtils.canEditSeparateObjectValue(
				ReflectionUIUtils.isValueImmutable(getSwingRenderer().getReflectionUI(), initialObjectValue),
				getObjectValueReturnMode(), canCommit());
	}

	protected void refreshEditorFormOnModification(final Form form) {
		ModificationStack childModificationStack = form.getModificationStack();
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				refreshEditorForm(form, false);
			}
		});
	}

	public void refreshEditorForm(Form editorForm, boolean refreshStructure) {
		encapsulatedObjectValueAccessor.set(getInitialObjectValue());
		editorForm.refresh(refreshStructure);
	}

	protected boolean shouldAcceptNewObjectValue(Object value) {
		return true;
	}

	protected void forwardEditorFormModificationsToParentObject(final Form form, boolean exclusiveLinkWithParent) {
		Accessor<Boolean> childModifAcceptedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return shouldAcceptNewObjectValue(getCurrentObjectValue());
			}
		};
		Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
			@Override
			public ValueReturnMode get() {
				return getObjectValueReturnMode();
			}
		};
		Accessor<Boolean> childValueReplacedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return (isObjectValueReplaced());
			}
		};
		Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				if (!canCommit()) {
					return null;
				}
				return createCommitModification(getCurrentObjectValue());
			}
		};
		Accessor<String> childModifTitleGetter = new Accessor<String>() {
			@Override
			public String get() {
				return getCumulatedModificationsTitle();
			}
		};
		Accessor<ModificationStack> masterModifStackGetter = new Accessor<ModificationStack>() {

			@Override
			public ModificationStack get() {
				ModificationStack result = getParentObjectModificationStack();
				if (result == null) {
					throw new ReflectionUIError();
				}
				return result;
			}
		};
		form.setModificationStack(new SlaveModificationStack(getSwingRenderer(), form, childModifAcceptedGetter,
				childValueReturnModeGetter, childValueReplacedGetter, commitModifGetter, childModifTitleGetter,
				masterModifStackGetter, exclusiveLinkWithParent));
	}

}
